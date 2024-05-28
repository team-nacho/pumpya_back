package com.sigma.pumpya.domain.entity

import com.sigma.pumpya.infrastructure.dto.PartyDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity(name = "party")
class Party (

    @Id
    val partyId: String,

    @Column(name = "party_name")
    val partyName: String,

    @Column(name = "total_cost")
    var totalCost: Double, //expected Cost By KRW

    @Column(name = "cost_list")
    var usedCurrencies: String //Serialize all currenies & cost
): BaseTimeEntity() {
    fun toDTO(): PartyDTO {
        val currenciesArray = this.usedCurrencies.split(',').toTypedArray()
        return PartyDTO(
            this.partyId,
            this.partyName,
            this.totalCost,
            currenciesArray
        )
    }
}