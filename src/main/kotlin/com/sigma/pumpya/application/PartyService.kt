package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import jakarta.transaction.Transactional
import org.springframework.core.style.ToStringCreator
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.util.*

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisPublisherService: RedisPublisherService
) {

    fun createParty(createPartyRequest: CreatePartyRequest): CreatePartyResponse {
        var partyId = UUID.randomUUID().toString()
        val partyName: String = "test party name"
        val partyAttributes =  Party(
            partyId,
            partyName,
            totalCost = 0.0,
            costList = ""
        )

        //partyRepository.save(party)

        val partyKey: String = "party:$partyId"
        redisTemplate.opsForHash<String, String>().putAll(partyKey, mapOf(
            "name" to partyName
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
            "name" to memberName,
            "costList" to ""
        ))

        return memberKey
    }
    fun addNewMemberInParty(partyKey: String, memberKey: String) {
        val partyMembersKey = "$partyKey:members"
        redisTemplate.opsForSet().add(partyMembersKey, memberKey)
    }

    /**TODO
     * 영수증을 받아온 후 DB에 저장, 총 금액 업데이트
     * id를 받아와서 redis에게 전송
     *
     */
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest) {
        val receiptId: String = "testId"
    }

    /**TODO
     *
     * db에서 삭제 구현
     *
     */
    fun deleteReceipt(receiptId: String) {
        //DB에서 삭제
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
         * 레디스에 삭제되기 전에 모든 영수증을 전부 계산해서 최신 반영해야함
         */
    }

}