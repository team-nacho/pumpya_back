package com.sigma.pumpya.infrastructure.repository

import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.infrastructure.dto.PartyDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PartyRepository : JpaRepository<Party, String> {
    fun findByPartyId(partyId: String): PartyDTO {
        val party: Party = findById(partyId).orElseThrow { Exception("Party not found with id $partyId") }
        return party.toDTO()
    }
}
