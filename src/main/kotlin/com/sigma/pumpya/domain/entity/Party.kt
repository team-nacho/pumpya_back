package com.sigma.pumpya.domain.entity

import jakarta.persistence.Id
import java.util.*
class Party(
    @field: Id
    val partyId: String,
    val partyName: String,
    var totalCost: Double, //expected Cost By KRW
    val costList: String //Serialize all currenies & cost
)