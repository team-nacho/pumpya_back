package com.sigma.pumpya.api.controller

import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.request.GetMembersRequest
import com.sigma.pumpya.api.request.GetReceiptRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.api.response.GetMembersResponse
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
        val createPartyResult = partyService.createParty(createPartyRequest)

        return CreatePartyResponse(
            createPartyResult.partyId,
            createPartyResult.partyName,
            createPartyResult.totalCost,
            createPartyResult.usedCurrencies
        )
    }

    /**TODO
     * 파티 아이디로 정산 결과 가져오기
     */
    @Operation(summary = "Get Pumppay Result")
    @GetMapping("/get-pumppay-result")
    fun getDutchResultWithPartyId() {}

    /**TODO
     * 파티 아이디로 영수증 가져오기
     * from DB
     */
    @GetMapping("/get-receipts")
    fun getReceiptsWithPartyId(
        @Valid getReceiptRequest: GetReceiptRequest
    ) {}

    @Operation(summary = "get members")
    @GetMapping("/get-members")
    fun getMembersWithPartyId(
        @Valid getMemberRequest: GetMembersRequest
    ): GetMembersResponse {
        return GetMembersResponse(partyService.getMembersWithPartyId(getMemberRequest.partyId))
    }
}