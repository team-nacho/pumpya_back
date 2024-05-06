package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private var redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val channelTopic: ChannelTopic,
    private val partyRepository: PartyRepository,
) {
    private val zSetOperations: ZSetOperations<String, String> = redisTemplate.opsForZSet()

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
            "name" to partyName
        ))

        redisTemplate.opsForSet().add("parties", partyKey);
        /**TODO
         * 동기 처리 관련 이슈 생길 수 있음
         */
        val memberKey = createMember(createPartyRequest.userName);
        addNewMemberInParty(partyKey, memberKey)

        return CreatePartyResponse(partyAttributes)
    }
    fun createMember(memberName: String): String {
        val memberId = UUID.randomUUID()
        val memberKey = "member:$memberId"

        redisTemplate.opsForHash<String, String>().putAll(memberKey, mapOf(
            "name" to memberName,
            "costList" to ""
        ))

        return memberKey
    }
    fun addNewMemberInParty(partyKey: String, memberKey: String) {
        val partyMembersKey = "$partyKey:members"
        redisTemplate.opsForSet().add(partyMembersKey, memberKey)
    }


    fun sendReceipt(createReceiptRequest: CreateReceiptRequest) {
        //레디스에게 발행 이 과정에서 파티의 총 금액을 계산해줘야함
        val topic: String = channelTopic.topic

        redisTemplate.convertAndSend(topic, createReceiptRequest)
    }

    fun deleteReceipt(receiptId: UUID) {

    }

    fun createNewMember(member: CreateNewMemberRequest) {

    }

    fun endParty(partyId: UUID) {
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