package com.sigma.pumpya.infrastructure.dto

import java.util.*

data class PartyDTO(
    val partyId: UUID,
    val partyName: String,
    val totalCost: Double,
    val usedCurrencies: String,
)
