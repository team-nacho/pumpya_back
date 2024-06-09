package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.controller.exception.CurrencyNotFoundException
import com.sigma.pumpya.api.controller.exception.PartyIdNotFoundException
import com.sigma.pumpya.api.controller.exception.ReceiptNotFoundException
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.CurrencyRepository
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList

@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository,
    private val objectMapper: ObjectMapper,
    private val redisPublisherService: RedisPublisherService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val partyRepository: PartyRepository,
    private val currencyService: CurrencyService,
    private val currencyRepository: CurrencyRepository,
){
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest): String {
        if(!partyRepository.existsById(createReceiptRequest.partyId)) {
            throw PartyIdNotFoundException()
        }

        val receiptId: String = UUID.randomUUID().toString()
        val partyKey: String = "party:${createReceiptRequest.partyId}"

        val newReceipt = Receipt(
            receiptId,
            createReceiptRequest.partyId,
            createReceiptRequest.author,
            createReceiptRequest.receiptName,
            createReceiptRequest.cost,
            createReceiptRequest.useCurrency,
            createReceiptRequest.useTag,
            objectMapper.writeValueAsString(createReceiptRequest.joins),
        )

        val res = receiptRepository.save(newReceipt)

        val partyInfo = redisTemplate.opsForHash<String, String>().entries(partyKey)

        val currencyList = try {
            objectMapper
                .readValue<Array<String>>(partyInfo["usedCurrencies"].toString(), Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }

        currencyList.add(createReceiptRequest.useCurrency)
        val currencyListToString = objectMapper.writeValueAsString(currencyList.distinct())


        redisTemplate.opsForHash<String, String>().put(partyKey, "usedCurrencies", currencyListToString)
        redisPublisherService.publishReceiptMessage(receiptId, Topic.RECEIPT_CREATED.name, objectMapper.writeValueAsString(res) )

        return receiptId
    }

    fun getReceiptsByPartyId(partyId : String): List<ReceiptDTO> {
        return findAllByPartyId(partyId)
    }
    fun deleteReceipt(receiptId: String) : String{
        if(receiptRepository.existsById(receiptId)) {
            throw ReceiptNotFoundException()
        }
        //DB에서 삭제
        //TODO 만약 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
        val receipt = receiptRepository.findById(receiptId)
        if (receipt.isPresent) {
            val partyId = receipt.get().partyId
            val useCurrency = receipt.get().useCurrency
            val partyKey = "party:$partyId"
            // 해당 통화에 대한 다른 영수증이 있는지 확인
            val otherReceipts = findAllByUseCurrency(useCurrency)
            if (otherReceipts.isEmpty()) {
                // 해당 파티에서 통화 목록 가져오기
                val partyInfo = redisTemplate.opsForHash<String, String>().entries(partyKey)
                val useCurrencies = objectMapper.readValue(partyInfo["usedCurrencies"], Array<String>::class.java)
                val newCurrencyList:Array<String> = arrayOf<String>()

                useCurrencies.forEach { it ->   if(it != useCurrency) { newCurrencyList + it } }
                val currencyListToString = objectMapper.writeValueAsString(newCurrencyList.distinct())

                redisTemplate.opsForHash<String, String>().put(partyKey, "usedCurrencies", currencyListToString)
            }
            // 영수증 삭제
            receiptRepository.deleteById(receiptId)
            return objectMapper.writeValueAsString(receipt.get())
        } else throw ReceiptNotFoundException()
    }

    /*
     * TODO
     *  아마 이 부분은 repository의 책임인 것 같음
     */


    fun findAllByUseCurrency(useCurrency: String) : List<ReceiptDTO>{
        if(currencyRepository.existsById(useCurrency))
            throw CurrencyNotFoundException()

        val receiptList = receiptRepository.findAll()
        val result = mutableListOf<ReceiptDTO>()
        for(receipt in receiptList) {
            if(receipt.useCurrency == useCurrency) {
                result.add(receipt.toDTO())
            }
        }
        if(receiptList.isEmpty()) return mutableListOf<ReceiptDTO>() //Error Emerge
        return result
    }

    fun findAllByPartyId(partyId: String): List<ReceiptDTO> {
        if(!partyRepository.existsById(partyId)) {
            throw PartyIdNotFoundException()
        }

        val receiptList = receiptRepository.findAll()
        val result = mutableListOf<ReceiptDTO>()
        for(receipt in receiptList) {
            if(receipt.partyId == partyId) {
                result.add(receipt.toDTO())
            }
        }
        if(receiptList.isEmpty()) return mutableListOf<ReceiptDTO>() //Error Emerge
        return result
    }
}