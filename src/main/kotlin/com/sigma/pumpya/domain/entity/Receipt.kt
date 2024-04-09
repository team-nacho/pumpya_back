package com.sigma.pumpya.domain.entity

import java.time.LocalDateTime
import java.util.*

class Receipt(
    val partyId: UUID,
    val name: String,
    val currency: String,
    val tag: String,
    val receipt: String,
    val join: Map<String, String>
)