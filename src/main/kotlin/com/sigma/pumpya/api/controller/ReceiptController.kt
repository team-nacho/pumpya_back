package com.sigma.pumpya.api.controller

import com.sigma.pumpya.application.ReceiptService
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name="receipt api")
@RestController("/receipt")
class ReceiptController(
    private val receiptService: ReceiptService
) {
    @GetMapping("/get-receipts/{partyId}")
    fun getReceiptsWithPartyId(
        @PathVariable partyId: String
    ): List<ReceiptDTO> {
        return receiptService.getReceiptsByPartyId(partyId)
    }
}