package com.sigma.pumpya.api.response

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sigma.pumpya.domain.entity.Member

data class GetPartyResponse(
    val partyId: String,
    val partyName: String,
    val usedCurrencies: List<String>,
    val members: List<String>
)
