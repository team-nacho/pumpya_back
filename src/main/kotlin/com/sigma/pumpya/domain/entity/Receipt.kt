package com.sigma.pumpya.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.*
@Entity
class Receipt(
    @field: Id
    val receiptId : String,
    val partyId: String,
    val name: String, //cost use description
    val cost : Double,
    val currency: String,
    val tag: String,
    val join: String //Serialize members list
)