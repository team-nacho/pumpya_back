package com.sigma.pumpya.api

import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.application.PartyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "party Api")
@RestController("/party")
class PartyController(
    private val partyService: PartyService,
) {

    @Operation(summary = "Create Party")
    @PostMapping("/create-party")
    fun createParty(
        @Valid @RequestBody createPartyRequest: CreatePartyRequest
    ): CreatePartyResponse {
        return partyService.createParty(createPartyRequest)
    }
    @Operation(summary = "Get Dutch Result")
    @GetMapping("/get-dutch-result")
    fun getDutchResultWithPartyId() {}

    @Operation(summary = "Create New User")
    @PostMapping("/create-new-member")
    fun createNewMemberToPartyWithPartyId() {}

    @GetMapping("/get-receipts")
    fun getReceiptsWithPartyId() {}
}