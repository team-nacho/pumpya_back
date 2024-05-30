package com.sigma.pumpya.domain.entity

import jakarta.persistence.*

@Entity(name = "tag")
class Tags (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    var tagId: Long? = null,

    @Column(name = "tag_name")
    val tagName: String
)