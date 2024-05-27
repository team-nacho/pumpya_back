package com.sigma.pumpya.infrastructure.dto

import com.sigma.pumpya.domain.entity.Currency
import com.sigma.pumpya.domain.entity.Tag
import java.util.*

data class ReceiptDTO(
    val receiptId : String,
    val partyId: String,
    val receiptName: String,
    val cost: Double,
    val joins: String,
    val useCurrency: String,
    val createdAt: Date,
    val useTag : String
)
