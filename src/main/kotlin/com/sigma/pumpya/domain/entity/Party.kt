package com.sigma.pumpya.domain.entity

import com.sigma.pumpya.infrastructure.dto.PartyDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity(name = "party")
class Party (
    @Id
    @Column(name = "party_id")
    val partyId: String,

    @Column(name = "party_name")
    val partyName: String,

    @Column(name = "party_arch")
    var partyArch: String
): BaseTimeEntity() {
    fun toDTO(): PartyDTO {
        return PartyDTO(
            this.partyId,
            this.partyName,
            this.partyArch
        )
    }
}