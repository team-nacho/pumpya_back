package com.sigma.pumpya.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.*
@Entity(name = "receipt")
class Receipt(
    @Id
    val receiptId : String,
    @Column(name = "party_id")
    val partyId: String,
    @Column(name = "receipt_name")
    val receiptName: String, //cost use description\
    @Column(name = "cost")
    val cost : Double,
    @Column(name = "use_currency")
    val useCrrency: String,
    @Column(name = "use_tag")
    val useTag: String,
    @Column(name = "join")
    val join: String //Serialize members list
) : BaseTimeEntity()