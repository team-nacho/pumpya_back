package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.dto.PartyDTO
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.CurrencyRepository
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import com.sigma.pumpya.infrastructure.repository.TagRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val redisPublisherService: RedisPublisherService,
    private val receiptService: ReceiptService,
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



    /**TODO
     * 영수증을 받아온 후 DB에 저장, 총 금액 업데이트
     * id를 받아와서 redis에게 전송
     *
     */
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest): String {
        val receiptId: String = UUID.randomUUID().toString()
        val partyKey: String = "party:${createReceiptRequest.partyId}"

        val newReceipt = Receipt(
            receiptId,
            partyKey,
            createReceiptRequest.author,
            createReceiptRequest.receiptName,
            createReceiptRequest.cost,
            objectMapper.writeValueAsString(createReceiptRequest.joins),
            createReceiptRequest.useCurrency,
            createReceiptRequest.useTag,
          )

        //receipt save in DB
        receiptService.saveReceipt(createReceiptRequest)
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
         *  Com?
         *  레디스에 삭제되기 전에 모든 영수증을 전부 계산해서 최신 반영해야함
         */
        pumppaya(partyId);
    }

    fun pumppaya(partyId : String) : Map<String, Map<String, Map<String, Double>>> {
        val receiptList: List<ReceiptDTO> = receiptService.findAllByPartyId(partyId)
        if (receiptList.isEmpty()) return emptyMap() // Exception Handler

        val mappingTable: MutableMap<String, Int> = mutableMapOf()
        val receiptResult: MutableMap<String, MutableMap<String, MutableMap<String, Double>>> = mutableMapOf()
        var memberIndex : Int = 0

        // 멤버 목록 초기화
        for (receipt in receiptList) {
            val members = receipt.joins.split(",").toSet()

            // receipt author 추가
            if (!mappingTable.containsKey(receipt.author)) {
                mappingTable[receipt.author] = memberIndex
                memberIndex++
            }
            for (member in members) {
                if (mappingTable.containsKey(member)) continue
                mappingTable[member] = memberIndex
                memberIndex++
            }
        }

        // 금액 계산
//        for (receipt in receiptList) {
//            val members = receipt.joins.split(",").toSet().size + 1 // 발행자 포함
//            val cost = receipt.cost / members
//            val currency = receipt.useCurrency
//            if (!receiptResult.containsKey(currency)) {
//                receiptResult[currency] = mutableMapOf()
//            }
//
//            for ((member, index) in mappingTable) {
//                if (!receiptResult[currency]!!.containsKey(member)) {
//                    receiptResult[currency]!![member] = mutableMapOf()
//                }
//                receiptResult[currency]!![member]!![receipt.author] = (receiptResult[currency]!![member]!![receipt.author] ?: 0.0) + cost
//            }
//        }
        for (receipt in receiptList) {
            val members = receipt.joins.split(",").toSet().size + 1 // 발행자 포함
            val cost = receipt.cost / members
            val currency = receipt.useCurrency
            val authorIndex = mappingTable[receipt.author] ?: continue // 발행자가 없으면 건너뛰기

            receiptResult.getOrPut(currency) { mutableMapOf() }
                .getOrPut(receipt.author) { mutableMapOf() }

            for ((member, index) in mappingTable) {
                if (member != receipt.author) {
                    receiptResult[currency]!![member]
                        ?.getOrPut(receipt.author) { 0.0 }
                        ?.plus(cost)
                }
            }
        }

        return receiptResult
    }


}