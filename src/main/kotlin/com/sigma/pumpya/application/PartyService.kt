package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.api.response.CreateReceiptResponse
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.enums.Topic
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import jakarta.transaction.Transactional
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
    @Autowired
    private val partyRepository: PartyRepository,
    private val receiptRepository : ReceiptRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisPublisherService: RedisPublisherService
) {

    fun createParty(createPartyRequest: CreatePartyRequest): CreatePartyResponse {
        var partyId = UUID.randomUUID().toString()
        val partyName: String = "test party name"
        val partyAttributes = Party(
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

        val memberKey = createMember(createPartyRequest.userName);
        addNewMemberInParty(partyKey, memberKey)

        //JPA
        saveParty(partyId, partyAttributes)

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
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest) : String {
        val receiptObject = Receipt(
            receiptId = createReceiptRequest.receiptId,
            partyId = createReceiptRequest.partyId,
            receiptName = createReceiptRequest.name,
            cost = createReceiptRequest.cost,
            useCrrency = createReceiptRequest.currency,
            useTag = "", // useTag가 CreateReceiptRequest에 없으므로 빈 문자열 또는 기본값을 설정
            join = createReceiptRequest.join.joinToString(","), // Array<String>을 콤마로 구분된 String으로 변환
            // createdAt을 LocalDateTime으로 변환
            // BaseEntity의 @CreatedDate와 @LastModifiedDate는 자동으로 처리되므로 여기서 직접 설정할 필요는 없음
        )

        // JPA 리포지토리를 사용해 데이터베이스에 저장
        receiptRepository.save(receiptObject)

        return receiptObject.receiptId
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

    fun saveParty(partyId: String, partyObject: Party) {
        //Jpa
        partyRepository.save(partyObject)

        //redis

    }
}