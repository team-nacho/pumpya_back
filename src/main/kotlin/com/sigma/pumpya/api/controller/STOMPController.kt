package com.sigma.pumpya.api.controller

import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.api.request.DeleteReceiptRequest
import com.sigma.pumpya.application.PartyService
import com.sigma.pumpya.application.ReceiptService
import com.sigma.pumpya.application.RedisPublisherService
import com.sigma.pumpya.infrastructure.enums.Topic
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional

@Tag(name="STOMP api")
@Controller
class STOMPController(
    private val partyService: PartyService,
    private val redisPublisherService: RedisPublisherService,
    private val receiptService: ReceiptService
) {
    @Operation(summary = "create receipt")
    @MessageMapping("/party/{partyId}/create")
    fun createReceipt(
        @DestinationVariable partyId: String,
        @Valid createReceiptRequest: CreateReceiptRequest
    ) {
        val receiptId: String = receiptService.saveReceipt(createReceiptRequest)
    }
    /**
    * TODO
    *  영수증 삭제
    *  요청이 들어오면 레디스에서 삭제 후
    *  최신 계산 결과를 레디스에 다시 업데이트
    * */
    @Operation(summary = "delete receipt")
    @MessageMapping("/party/{partyId}/delete")
    @Transactional
    fun deleteReceipt(
        @DestinationVariable partyId: String,
        @Valid request: DeleteReceiptRequest
    ) {
        //db에서 삭제 후 레디스에 없데이트
        val receipt = receiptService.deleteReceipt(request.receiptId);
        redisPublisherService.publishReceiptMessage(request.receiptId, Topic.RECEIPT_DELETED.name, receipt)
    }
    @Operation(summary = "create member")
    @MessageMapping("/party/{partyId}/new-member")
    fun createNewMember(
        @DestinationVariable partyId: String,
        @Valid request: CreateNewMemberRequest
    ) {
        val partyKey = "party:$partyId"
        val memberKey = partyService.createMember(request.name)
        partyService.addNewMemberInParty(partyKey, memberKey)
        redisPublisherService.publishMemberMessage(partyId, Topic.MEMBER_REGISTERED.name, request.name)
    }

    @Operation(summary = "end party")
    @MessageMapping("/party/{partyId}/end")
    fun endParty(
        @DestinationVariable partyId: String,
    ) {
        partyService.endParty(partyId)
        redisPublisherService.publishPartyMessage(partyId, Topic.PARTY_ENDED.name)
    }
}