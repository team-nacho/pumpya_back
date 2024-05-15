package com.sigma.pumpya.infrastructure.dto

import java.util.*

data class PartyDTO(
    val partyId: String,
    val partyName: String,
    val totalCost: Double,
    val usedCurrencies: String,
)
