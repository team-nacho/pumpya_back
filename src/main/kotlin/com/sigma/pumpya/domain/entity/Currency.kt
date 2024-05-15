package com.sigma.pumpya.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "currency")
class Currency (
    @field: Id
    val currencyId : String, //currency Symbol
    @Column(name = "country_name")
    val country : String //Currency Origin
)