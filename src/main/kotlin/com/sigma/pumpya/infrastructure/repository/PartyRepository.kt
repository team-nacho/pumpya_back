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

        redisTemplate.opsForHash<String, String>()
    }
    fun saveMember(partyId: UUID, memberId: UUID, memberAttributes: Member) {

    }

    fun addPartyCurrency(partyId: UUID, receipt: Receipt) {
        
    }
}
