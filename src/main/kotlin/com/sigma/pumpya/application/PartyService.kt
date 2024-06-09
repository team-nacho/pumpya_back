package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.controller.PartyController
import com.sigma.pumpya.api.controller.exception.PartyIdNotFoundException
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
        if(!partyRepository.existsById(partyId)) {
            throw PartyIdNotFoundException()
        }

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
        if(!partyRepository.existsById(partyId)) {
            throw PartyIdNotFoundException()
        }
        val partyKey: String = "party:$partyId"
        val partyMembersKey = "$partyKey:members"

        redisTemplate.delete(partyKey)

        val partyMembers = redisTemplate.opsForSet().members(partyMembersKey)!!
        for(memberId in partyMembers) {
            val memberKey = "member:$memberId"
            redisTemplate.delete(memberKey)
        }
        redisTemplate.delete(partyMembersKey)
        redisTemplate.opsForSet().remove("parties", partyKey)

        enhancedPump(partyId);
    }

    fun pumppayaResult(getPumppayaResultRequest: GetPumppayaResultRequest) : GetPumppayaResultResponse {
        val input = enhancedPump(getPumppayaResultRequest.partyId)
        return GetPumppayaResultResponse(input)
    }

    fun calReceipt(resultMap: MutableMap<String, MutableMap<String, Double>>, author: String, join: String, cost: Double) {
        if (author == join) return  // 혼자

        // 2개의 맵을 만들어서 진행
        val authorToJoin = resultMap.getOrPut(author) { mutableMapOf() }
        val joinToAuthor = resultMap.getOrPut(join) { mutableMapOf() }

        // 계산
        val amount = authorToJoin.getOrDefault(author, 0.0)

        if (amount > 0) {
            if (amount > cost) {
                authorToJoin[join] = amount - cost
            } else if (amount < cost) {
                joinToAuthor[author] = cost - amount
                authorToJoin.remove(join)
            } else {
                authorToJoin.remove(author)
            }
        } else {
            joinToAuthor[author] = joinToAuthor.getOrDefault(author, 0.0) + cost
        }
    }
    fun enhancedPump(partyId: String): MutableMap<String, MutableMap<String,MutableMap<String,Double>>> {
        if(!partyRepository.existsById(partyId)) throw PartyIdNotFoundException()
        // get receipts
        val receipts = receiptService.getReceiptsByPartyId(partyId)

        // 통화별 영수증 리스트
        val currencyReceiptMap = mutableMapOf<String, MutableList<ReceiptDTO>>()
        for(receipt in receipts) {
            val block = currencyReceiptMap.getOrPut(receipt.useCurrency) { mutableListOf() }
            block.add(receipt)
        }

        val result = mutableMapOf<String, MutableMap<String, MutableMap<String, Double>>>()
        currencyReceiptMap.forEach{
            // 초기 결과에는 아무 정보도 없으므로 추가
            result.getOrPut(it.key) { mutableMapOf() }

            val resultMap = mutableMapOf<String, MutableMap<String, Double>>()
            for(receipt in it.value) {
                val joins = objectMapper.readValue(receipt.joins, Array<String>::class.java)

                val cost = receipt.cost / (joins.size + 1)
                for(join in joins) {
                    calReceipt(resultMap, receipt.author, join, cost)
                }
            }

            result[it.key] = resultMap
        }
        //통화별 정산 테이블
        return result
    }
}