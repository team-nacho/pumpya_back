package com.sigma.pumpya.api.response

import com.sigma.pumpya.domain.entity.Member
import com.sigma.pumpya.domain.entity.Party
import java.util.*

data class CreatePartyResponse(
    var partyData: Party
)