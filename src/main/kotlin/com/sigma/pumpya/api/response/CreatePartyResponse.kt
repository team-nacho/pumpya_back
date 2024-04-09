package com.sigma.pumpya.api.response

import com.sigma.pumpya.domain.entity.Member
import java.util.*

data class CreatePartyResponse(
    var id: UUID,
    var partyName: String,
    var numberOfMembers: Int,
    var members: Map<String, Double>,
)