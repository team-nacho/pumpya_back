package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValues
import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {

    fun createParty(createPartyRequest: CreatePartyRequest): CreatePartyResponse {
        var partyId = UUID.randomUUID()
        val partyName: String = "test party name"
        val partyAttributes =  Party(
            partyId,
            partyName,
            totalCost = 0.0,
            costList = ""
        )

        val partyKey: String = "party:$partyId"
        redisTemplate.opsForHash<String, String>().putAll(partyKey, mapOf(
            "name" to partyName,
            "usedCurrencies" to ""
        ))

        redisTemplate.opsForSet().add("parties", partyKey);

        val memberKey = createMember(createPartyRequest.userName);
        addNewMemberInParty(partyKey, memberKey)

        return CreatePartyResponse(partyAttributes)
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
        return memberSet.mapNotNull { memberId ->
            val memberKey = "member:$memberId"
            redisTemplate.opsForHash<String, String>().entries(memberKey)["name"]
        }
    }

    /**TODO
     * 영수증을 받아온 후 DB에 저장, 총 금액 업데이트
     * id를 받아와서 redis에게 전송
     *
     */
    fun getPartyInfo(partyKey:String): Map<String, String> {
        return redisTemplate.opsForHash<String, String>().entries(partyKey)
    }
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest) {
        val receiptId: String = "testId"
        val partyKey: String = "party:${createReceiptRequest.partyId}"

        //TODO saveDB
        //get party info. if not exist currency, add
        val partyInfo = getPartyInfo(partyKey)
        val currencyList = objectMapper
            .readValue<Array<String>>(partyInfo["usedCurrencies"], Array<String>::class.java).toMutableList()

        //TODO 바꾸면 좋지만 일단 넣어두고 중복제거
        currencyList.add(createReceiptRequest.currency)
        val currencyListToString = currencyList.distinct().toString()

        redisTemplate.opsForHash<String, String>().put(partyKey, "usedCurrencies", currencyListToString)
    }
    fun deleteReceipt(receiptId: String) {
        //DB에서 삭제
        //TODO 만약 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
    }

    /**TODO
     *
     * db에서 삭제 구현
     * 삭제 전 해당 영수증에 관련 있는 모든 데이터 업데이트
     *
     */


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
         * 레디스에 삭제되기 전에 모든 영수증을 전부 계산해서 최신 반영해야함
         */
    }

}