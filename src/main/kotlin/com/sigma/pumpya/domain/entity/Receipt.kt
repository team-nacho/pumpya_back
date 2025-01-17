package com.sigma.pumpya.domain.entity

import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(name = "receipt")
class Receipt(
    @Id
    @Column(name = "receipt_id")
    val receiptId : String,

    @Column(name = "party_id")
    val partyId: String,

    @Column(name = "author")
    val author : String,

    @Column(name = "receipt_name")
    val receiptName: String, //cost use description

    @Column(name = "cost")
    val cost : Double,

    @Column(name = "use_currency")
    val useCurrency: String,

    @Column(name = "use_tag")
    val useTag: String,

    @Column(name = "joins")
    val joins: String //Serialize members list
    
) : BaseTimeEntity() {
    fun toDTO(): ReceiptDTO {
//        val datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.createdAt!!.toLong()), ZoneId.of("UTC+9")).format(
//            DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss".toString()))
        return ReceiptDTO(
            this.receiptId,
            this.partyId,
            this.author,
            this.receiptName,
            this.cost,
            this.useCurrency,
            this.useTag,
            this.joins,
            this.createdAt!!.toLong()
        )
    }
}