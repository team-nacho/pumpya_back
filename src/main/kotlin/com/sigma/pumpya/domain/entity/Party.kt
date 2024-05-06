package com.sigma.pumpya.domain.entity

import jakarta.persistence.Id
import java.util.*
class Party(
    @field: Id
    val id: UUID,
    val partyName: String,
    val totalCost: Double,
    val costList: String
)