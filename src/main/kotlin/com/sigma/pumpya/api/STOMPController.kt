package com.sigma.pumpya.api

import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.application.PartyService
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*


@Controller
class STOMPController(
    private val partyService: PartyService,
) {
    @MessageMapping("/party/{partyId}/create")
    fun createReceipt(
        @DestinationVariable partyId: UUID,
        @Valid createReceiptRequest: CreateReceiptRequest
    ) {
        partyService.sendReceipt(createReceiptRequest)
    }
    @MessageMapping("/party/{partyId}/end")
    fun endParty(
        @DestinationVariable partyId: UUID,
    ) {
        //레디스에서 삭제 후 db에 영구 저장
        partyService.endParty(partyId)
    }

    @MessageMapping("/party/{partyId}/delete")
    fun deleteReceipt(
        @DestinationVariable partyId: UUID,
        @Valid receiptId: UUID
    ) {
        //db에서 삭제 후 레디스에 없데이트
        partyService.deleteReceipt(receiptId);
    }

    @MessageMapping("/party/{partyId}/new-member")
    fun createNewMember(
        @DestinationVariable partyId: UUID,
        @Valid member: CreateNewMemberRequest
    ) {
        /*
        레디스에 멤버 업데이트
         */
        partyService.createNewMember(member);
    }


}