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
        /**
         * 파티 정보를 redis에 올려야함
         * 이때 초기 멤버의 정보와 같이 올림
         */
        val partyAttributes =  Party(
            partyId,
            partyName,
            totalCost = 0.0,
            costList = mapOf()
        )
        partyRepository.saveParty(partyId, partyAttributes)

        // 초기 멤버 추가

        addNewMemberInParty(partyId, createPartyRequest.userName)
        // 각 통화별 사용 금액 리스트 초기화 (null로 설정)
        val currencies = listOf(null)
        val initialCurrencyCosts = currencies.associateWith { null }
        val currencyKey = "party:$partyId:currencyCosts"
        redisTemplate.opsForHash<String, String>().putAll(currencyKey, initialCurrencyCosts)

        return CreatePartyResponse(partyId, partyName, 1, mapOf(createPartyRequest.userName to 0.0))
    }
    fun addNewMemberInParty(partyId: UUID, memberName: String) {
        val memberId = UUID.randomUUID()
        val memberAttributes = Member(memberName , 0.0)
        partyRepository.saveMember(partyId, memberId, memberAttributes)
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

    fun endParty(partyId: UUID) {}

}