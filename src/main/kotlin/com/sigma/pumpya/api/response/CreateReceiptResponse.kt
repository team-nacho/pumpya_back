package com.sigma.pumpya.api.response

import com.sigma.pumpya.domain.entity.Member
import java.time.LocalDateTime
import java.util.UUID

data class CreateReceiptResponse(
    val receiptId: String,
    val name: String, //where
    val author: String, //receipt maker
    val cost: Double, //how mush
    val currency: String, //what currency
    val createDate: LocalDateTime, //when
    val join: String //whom
)
