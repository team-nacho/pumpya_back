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
    private val partyRepository: PartyRepository,
    private val receiptRepository : ReceiptRepository,
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

    /**TODO
     * 영수증을 받아온 후 DB에 저장, 총 금액 업데이트
     * id를 받아와서 redis에게 전송
     *
     */
    fun getPartyInfo(partyKey:String): Map<String, String> {
        //hash에서 name, costList
        return redisTemplate.opsForHash<String, String>().entries(partyKey)
    }
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest): String {
        val receiptId: String = UUID.randomUUID().toString()
        val partyKey: String = "party:${createReceiptRequest.partyId}"

        //TODO saveDB
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
        //get party info. if not exist currency, add
        val partyInfo = getPartyInfo(partyKey)

        val currencyList = try {
            objectMapper
                .readValue<Array<String>>(partyInfo["usedCurrencies"].toString(), Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }

        //TODO 바꾸면 좋지만 일단 넣어두고 중복제거
        currencyList.add(createReceiptRequest.useCurrency)
        val currencyListToString = objectMapper.writeValueAsString(currencyList.distinct())

        redisTemplate.opsForHash<String, String>().put(partyKey, "usedCurrencies", currencyListToString)
        redisPublisherService.publishReceiptMessage(receiptId, Topic.RECEIPT_CREATED.name, objectMapper.writeValueAsString(newReceipt) )

        return receiptId
    }
    fun deleteReceipt(receiptId: String) {
        //DB에서 삭제
        //TODO 만약 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
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