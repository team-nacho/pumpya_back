package com.sigma.pumpya.api

import com.sigma.pumpya.api.request.CreateNewMemberRequest
import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.application.PartyService
import com.sigma.pumpya.application.RedisPublisherService
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*


@Controller
class STOMPController(
    private val partyService: PartyService,
    private val redisPublisherService: RedisPublisherService
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
        partyService.endParty(partyId)
        /**TODO
         * 파티 종료 후 redis에 종료 메세지 발행
         */
//        redisPublisherService.publishMessage("")
    }

    /*
    * TODO
    *  영수증 삭제
    *  요청이 들어오면 레디스에서 삭제 후
    *  최신 계산 결과를 레디스에 다시 업데이트
    * */
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
        @Valid request: CreateNewMemberRequest
    ) {
        val partyKey = "party:$partyId"
        val memberKey = partyService.createMember(request.name)
        partyService.addNewMemberInParty(partyKey, memberKey)
    }


}