package com.sigma.pumpya.api.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sigma.pumpya.api.request.CreatePartyRequest
import com.sigma.pumpya.api.request.GetMembersRequest
import com.sigma.pumpya.api.request.GetPumppayaResultRequest
import com.sigma.pumpya.api.response.CreatePartyResponse
import com.sigma.pumpya.api.response.GetMembersResponse
import com.sigma.pumpya.api.response.GetPartyResponse
import com.sigma.pumpya.api.response.GetPumppayaResultResponse
import com.sigma.pumpya.application.PartyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

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
    /**TODO
     * Com?
     * 파티 아이디로 정산 결과 가져오기
     */
    //TODO
    @Operation(summary = "Get Pumppay Result")
    @GetMapping("/get-pumppay-result/{partyId}")
    fun getDutchResultWithPartyId(getPumppayaResultRequest: GetPumppayaResultRequest) : GetPumppayaResultResponse {
        val result = partyService.pumppayaResult(getPumppayaResultRequest)
        return result
    }

    @Operation(summary = "get members")
    @PostMapping("/get-members")
    fun getMembersWithPartyId(
        @Valid @RequestBody getMemberRequest: GetMembersRequest
    ): GetMembersResponse {
        val result = GetMembersResponse(partyService.getMembersWithPartyId(getMemberRequest.partyId))
        if(result.members.isEmpty()) { return GetMembersResponse(emptyList()) }
        return result
    }

    @Operation(summary = "get party with party Id")
    @GetMapping("/get-party/{partyId}")
    fun getPartyWithPartyId(
        @PathVariable partyId: String
    ): GetPartyResponse {
        val partyKey: String = "party:${partyId}"
        val partyInfo = partyService.getPartyInfo(partyKey)
        val members = partyService.getMembersWithPartyId(partyId)
        return GetPartyResponse(
            partyId,
            partyInfo["name"]!!,
            jacksonObjectMapper().readValue(partyInfo["usedCurrencies"]!!.toString(), Array<String>::class.java).toMutableList(),
            members
        )
    }
}