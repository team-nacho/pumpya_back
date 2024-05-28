package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValues
import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.api.response.CreateReceiptResponse
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.dto.PartyDTO
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.CurrencyRepository
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import com.sigma.pumpya.infrastructure.repository.TagRepository
import jakarta.transaction.Transactional
import jakarta.validation.constraints.Null
import org.apache.commons.lang3.ObjectUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.style.ToStringCreator
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val receiptRepository : ReceiptRepository,
    private val currencyRepository: CurrencyRepository,
    private val tagRepository: TagRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val redisPublisherService: RedisPublisherService
) {

    fun createParty(createPartyRequest: CreatePartyRequest): PartyDTO {
        var partyId = UUID.randomUUID().toString()
        val partyName: String = "test party name"
        //Party DTO
        val partyAttributes = Party(
            partyId,
            partyName,
            totalCost = 0.0,
            usedCurrencies = ""
        )

        val partyKey: String = "party:$partyId"
        redisTemplate.opsForHash<String, String>().putAll(partyKey, mapOf(
            "name" to partyName,
            "usedCurrencies" to "[]"
        ))

        redisTemplate.opsForSet().add("parties", partyKey);

        val memberKey = createMember(createPartyRequest.userName);
        addNewMemberInParty(partyKey, memberKey)

        //JPA
        partyRepository.save(partyAttributes)

        return partyAttributes.toDTO()
    }

    fun createMember(memberName: String): String {
        val memberId = UUID.randomUUID()
        val memberKey = "member:$memberId"

        redisTemplate.opsForHash<String, String>().putAll(memberKey, mapOf(
            "name" to memberName
        ))

        return memberKey
    }

    fun addNewMemberInParty(partyKey: String, memberKey: String) {
        val partyMembersKey = "$partyKey:members"
        redisTemplate.opsForSet().add(partyMembersKey, memberKey)
    }

    fun getMembersWithPartyId(partyId: String): List<String> {
        val partyMembersKey = "party:$partyId:members"
        val memberSet: Set<String> = redisTemplate.opsForSet().members(partyMembersKey) ?: emptySet()
        var memberList: MutableList<String> = mutableListOf()

        for(memberId in memberSet) {
            val name = redisTemplate.opsForHash<String, String>().entries(memberId)["name"]!!
            memberList.add(name)
        }
        return memberList
    }

    fun getPartyInfo(partyKey:String): Map<String, String> {
        return redisTemplate.opsForHash<String, String>().entries(partyKey)
    }

    fun getReceiptsByPartyId(partyId : String): List<Receipt> {
        return receiptRepository.findAllByPartyId(partyId)
    }

    /**TODO
     * 영수증을 받아온 후 DB에 저장, 총 금액 업데이트
     * id를 받아와서 redis에게 전송
     *
     */
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest): String {
        val receiptId: String = UUID.randomUUID().toString()
        val partyKey: String = "party:${createReceiptRequest.partyId}"

        val newReceipt = ReceiptDTO(
            receiptId,
            createReceiptRequest.partyId,
            createReceiptRequest.receiptName,
            createReceiptRequest.cost,
            objectMapper.writeValueAsString(createReceiptRequest.joins),
            createReceiptRequest.useCurrency,
            createReceiptRequest.createdAt,
            createReceiptRequest.tag
          )
        
        receiptRepository.save(receiptObject)
        //get party info. if not exist currency, add
        val partyInfo = getPartyInfo(partyKey)

        val currencyList = try {
            objectMapper
                .readValue<Array<String>>(partyInfo["usedCurrencies"].toString(), Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
        
        currencyList.add(createReceiptRequest.useCurrency)
        val currencyListToString = objectMapper.writeValueAsString(currencyList.distinct())


        redisTemplate.opsForHash<String, String>().put(partyKey, "usedCurrencies", currencyListToString)
        redisPublisherService.publishReceiptMessage(receiptId, Topic.RECEIPT_CREATED.name, objectMapper.writeValueAsString(newReceipt) )

        return receiptId
    }

    fun deleteReceipt(receiptId: String) : String{
        //DB에서 삭제
        //TODO 만약 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
        val receipt = receiptRepository.findById(receiptId)
        if (receipt.isPresent) {
            val partyId = receipt.get().partyId
            val useCurrency = receipt.get().useCurrency

            // 해당 통화에 대한 다른 영수증이 있는지 확인
            val otherReceipts = receiptRepository.findAllByUseCurrency(useCurrency)
            if (otherReceipts.isEmpty()) {
                // 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
                val partyObject = partyRepository.findById(partyId)
                val useCurrencies = partyObject.get().costList
                if( useCurrencies.contains(useCurrency) ) {
                    val currencyList = useCurrencies.split(",").filter { currencyPair ->
                        val (currency, _) = currencyPair.split(":")
                        currency != useCurrency
                    }.joinToString(",")

                    val updatedParty = Party(
                        partyId = partyObject.get().partyId,
                        partyName = partyObject.get().partyName,
                        totalCost = partyObject.get().totalCost,
                        costList = currencyList
                    )
                    partyRepository.save(updatedParty)
                }
            }
            // 영수증 삭제
            receiptRepository.deleteById(receiptId)
            return "success"
        } else {
            return "fail"
        }
    }

    fun endParty(partyId: String) {
        val partyKey: String = "party:$partyId"
        val partyMembersKey = "$partyKey:members"

        redisTemplate.opsForHash<String, String>().delete(partyKey)

        val partyMembers = redisTemplate.opsForSet().members(partyMembersKey) ?: emptySet()
        for(memberId in partyMembers) {
            val memberKey = "member:$memberId"
            redisTemplate.opsForHash<String, String>().delete(memberKey)
        }
        redisTemplate.opsForSet().remove(partyMembersKey)
        redisTemplate.opsForSet().remove("parties", partyKey)

        /**TODO
         * Com?
         * 레디스에 삭제되기 전에 모든 영수증을 전부 계산해서 최신 반영해야함
         */
        pumppaya(partyId);
    }

    fun saveParty(partyId: String, partyObject: Party) {
        //Jpa
        partyRepository.save(partyObject)

        //redis

    }

    fun pumppaya(partyId : String) : Map<String, Map<String, Array<Array<Double>>>> {
        val receiptList: List<Receipt> = receiptRepository.findAllByPartyId(partyId)
        if (receiptList.isEmpty()) return emptyMap() // Exception Handler

        val mappingTable: MutableMap<String, Int> = mutableMapOf()
        val receiptResult: MutableMap<String, Array<Array<Double>>> = mutableMapOf()
        var memberCount = 0

        // 멤버 목록 초기화
        for (receipt in receiptList) {
            val members = receipt.joins.split(",").toSet()
            for (member in members) {
                if (mappingTable.containsKey(member)) continue
                mappingTable[member] = memberCount
                memberCount++
            }
        }

        // 초기 결과 배열 생성
        for ((currency, _) in receiptResult) {
            receiptResult[currency] = Array(memberCount) { Array(memberCount) { 0.0 } }
        }

        // 금액 계산
        for (receipt in receiptList) {
            val members = receipt.joins.split(",").toSet().size + 1 // 발행자 포함
            val cost = receipt.cost / members
            val currency = receipt.useCurrency
            if (!receiptResult.containsKey(currency)) {
                receiptResult[currency] = Array(memberCount) { Array(memberCount) { 0.0 } }
            }

            val authorIndex = mappingTable[receipt.author]!!
            for ((member, index) in mappingTable) {
                if (member != receipt.author) {
                    receiptResult[currency]!![index][authorIndex] += cost
                }
            }
        }

        // 결과 맵 생성
        val result: MutableMap<String, Map<String, Array<Array<Double>>>> = mutableMapOf()
        for ((currency, currencyResult) in receiptResult) {
            val currencyMap: MutableMap<String, Array<Array<Double>>> = mutableMapOf()
            for (i in currencyResult.indices) {
                val fromMember = mappingTable.entries.first { it.value == i }.key
                currencyMap[fromMember] = currencyResult
            }
            result[currency] = currencyMap
        }

        return result
    }
}