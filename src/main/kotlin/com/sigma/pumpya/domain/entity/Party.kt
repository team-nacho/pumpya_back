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
    @Column(name = "cost_list")
    var usedCurrencies: String //Serialize all currenies & cost
): BaseTimeEntity() {
    fun stringToMap(costListString: String, separator: String = ","): Map<String, Double> {
        val costMap = mutableMapOf<String, Double>()

        if (costListString.isNotBlank()) {
            val entries = costListString.split(separator)
            for (entry in entries) {
                val (key, value) = entry.split(":")
                costMap[key.trim()] = value.trim().toDouble()
            }
        }

        return costMap
    }
    fun toDTO(): PartyDTO {
        val currenciesArray = this.usedCurrencies.split(',').toTypedArray()
        return PartyDTO(
            this.partyId,
            this.partyName,
            currenciesArray
        )
    }
}