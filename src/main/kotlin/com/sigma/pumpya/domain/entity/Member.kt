package com.sigma.pumpya.domain.entity

import java.util.UUID

class Member(
    var memberId: UUID,
    var partyId: UUID,
    var name: String,
    var costList: String,
)