package com.sigma.pumpya.infrastructure.dto

import java.util.*

data class ReceiptDto(
    val partyId: UUID,
    val receiptName: String,
    val cost: Double,
    val join: String,
    val currency: String,
    val createDate: Date,
)
