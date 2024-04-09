package com.sigma.pumpya.api.request

data class CreateReceiptRequest(
    val sender: String,
    val message: String,
)