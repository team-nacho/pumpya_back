package com.sigma.pumpya.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Tag (
    @field: Id
    val tagId: Long,
    val tagName: String
)