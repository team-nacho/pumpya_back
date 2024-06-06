package com.sigma.pumpya.infrastructure.dto

data class ReceiptDTO(
    val receiptId : String,
    val partyId: String,
    val author : String,
    val receiptName: String,
    val cost: Double,
    val useCurrency: String,
    val useTag : String,
    val joins: String,
    val createdAt: String,
)