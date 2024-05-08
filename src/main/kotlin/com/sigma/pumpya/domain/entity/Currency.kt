package com.sigma.pumpya.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Currency (
    @field: Id
    val currencyId : String, //currency Symbol
    val country : String //Currency Origin
)