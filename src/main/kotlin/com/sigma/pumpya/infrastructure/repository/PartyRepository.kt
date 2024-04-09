package com.sigma.pumpya.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository
import java.time.ZoneOffset
import java.util.*

@Repository
class PartyRepository @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {
    private val zSetOperations: ZSetOperations<String, String> = redisTemplate.opsForZSet()

    fun saveParty(partyId: UUID, partyAttributes: Party) {
        val partyKey = "party:$partyId"

        // 파티 속성을 JSON 문자열로 변환하여 Redis에 저장
        val partyJson = objectMapper.writeValueAsString(partyAttributes)
        redisTemplate.opsForValue().set(partyKey, partyJson)
    }
    fun saveMember(partyId: UUID, memberId: UUID, memberAttributes: Member) {
        val memberKey = "member:$memberId"

        // 멤버 정보를 JSON 문자열로 변환하여 Redis에 저장
        val memberJson = objectMapper.writeValueAsString(memberAttributes)
        redisTemplate.opsForValue().set(memberKey, memberJson)

        // 파티와 멤버의 연결 정보를 저장
        val partyMemberKey = "party:$partyId:members"
        redisTemplate.opsForSet().add(partyMemberKey, memberId.toString())
    }

    fun addPartyCurrency(partyId: UUID, receipt: Receipt) {
        
    }
}
