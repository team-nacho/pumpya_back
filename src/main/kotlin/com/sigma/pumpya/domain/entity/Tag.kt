package com.sigma.pumpya.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "tag")
class Tag (
    @field: Id
    val tagId: Long,
    @Column(name = "tag_name")
    val tagName: String
)