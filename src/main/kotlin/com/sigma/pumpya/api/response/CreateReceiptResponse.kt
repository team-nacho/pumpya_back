package com.sigma.pumpya.api.response

import com.sigma.pumpya.domain.entity.Member
import java.time.LocalDateTime
import java.util.UUID

data class CreateReceiptResponse(
    val receiptId: UUID,
    val name: String,
    val author: Member,
    val cost: Double,
    val currency: String,
    val createDate: LocalDateTime,
    val join: Array<Member>
)
