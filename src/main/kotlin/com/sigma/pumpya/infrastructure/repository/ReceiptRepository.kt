package com.sigma.pumpya.infrastructure.repository

import com.sigma.pumpya.domain.entity.Receipt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReceiptRepository : JpaRepository<Receipt, String> {
    fun findAllByPartyId(partyId: String): List<Receipt>
}
