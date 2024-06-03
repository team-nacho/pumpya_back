package com.sigma.pumpya.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Id
import java.util.UUID

class Member(
    @Id
    @Column(name="member_id")
    var memberId: String,

    @Column(name="party_id")
    var partyId: String,

    @Column(name="name")
    var name: String,

    @Column(name="cost_list")
    var costList: String,
)