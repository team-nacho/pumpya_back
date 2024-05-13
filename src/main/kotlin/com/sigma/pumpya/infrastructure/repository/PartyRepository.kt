package com.sigma.pumpya.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository
import java.time.ZoneOffset
import java.util.*

@Repository
interface PartyRepository : JpaRepository<Party, String> {

    fun saveParty(partyId: String, partyAttributes: Party) {
        val partyKey = "party:$partyId"
    }
    fun saveMember(partyId: String, memberId: String, memberAttributes: Member) {

    }

    fun addPartyCurrency(partyId: String, receipt: Receipt) {
        
    }
}
