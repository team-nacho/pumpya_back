package com.sigma.pumpya.infrastructure.dto

import com.sigma.pumpya.domain.entity.Currency
import com.sigma.pumpya.domain.entity.Tag
import java.util.*

data class ReceiptDTO(
    val receiptId : String,
    val partyId: String,
    val author : String,
    val receiptName: String,
    val cost: Double,
    val useCurrency: String,
    val useTag : String,
    val joins: String,
    val createdAt: Date,
)
