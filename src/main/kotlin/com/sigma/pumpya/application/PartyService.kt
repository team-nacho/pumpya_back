package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.GetPumppayaResultRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.api.response.GetPumppayaResultResponse
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.util.ListParser
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val receiptService: ReceiptService,
    private val listParser: ListParser,
    private val objectMapper: ObjectMapper,
) {
    fun createParty(createPartyRequest: CreatePartyRequest): CreatePartyResponse {
        val partyId = UUID.randomUUID().toString()
        val name = listParser.randomNameCreator()
        val partyName: String = if (name.isNotEmpty()) name else "TempPartyName"
        //Party DTO
        val partyAttributes = Party(
            partyId,
            partyName,
        )

        val partyKey: String = "party:$partyId"
        redisTemplate.opsForHash<String, String>().putAll(partyKey, mapOf(
            "name" to partyName,
            "usedCurrencies" to "[]"
        ))

        redisTemplate.opsForSet().add("parties", partyKey);

        val memberKey = createMember(createPartyRequest.userName);
        addNewMemberInParty(partyKey, memberKey)
        val partyInfo = getPartyInfo(partyKey)
        //JPA
        partyRepository.save(partyAttributes)

        return CreatePartyResponse(
            partyAttributes.partyId,
            partyAttributes.partyName,
            objectMapper.readValue(partyInfo["usedCurrencies"], Array<String>::class.java)
        )
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

        pumppaya(partyId);
    }

    fun PumppayaResult(getPumppayaResultRequest: GetPumppayaResultRequest) : GetPumppayaResultResponse {
        val input = pumppaya(getPumppayaResultRequest.partyId)
        return GetPumppayaResultResponse(input)
    }


    fun pumppaya(partyId : String) : Map<String, Map<String, Map<String, Double>>> {
        // 정상적으로 작동하는지 알기 위해서 디버깅 추가
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

        //금액계산
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
        // 금액 전송 정보 계산
        val transferInfo: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
        for (currency in receiptResult.keys) {
            for (sender in receiptResult[currency]!!.keys) {
                for (receiver in receiptResult[currency]!![sender]!!.keys) {
                    val amount = receiptResult[currency]!![sender]!![receiver] ?: 0.0
                    if (amount > 0) {
                        transferInfo.getOrPut(sender) { mutableMapOf() }[receiver] = amount
                    }
                }
            }
        }

        return receiptResult
    }
}