package com.sigma.pumpya.application

import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository

class ReceiptService (
    private val receiptRepository: ReceiptRepository
){
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest) {
        // CreateReceiptRequest에서 받은 정보를 Receipt 엔티티 객체로 변환
        val receiptObject = Receipt(
            receiptId = createReceiptRequest.receiptId,
            partyId = createReceiptRequest.partyId,
            receiptName = createReceiptRequest.name,
            cost = createReceiptRequest.cost,
            useCrrency = createReceiptRequest.currency,
            useTag = "", // useTag가 CreateReceiptRequest에 없으므로 빈 문자열 또는 기본값을 설정
            joins = createReceiptRequest.joins.joinToString(","), // Array<String>을 콤마로 구분된 String으로 변환
            // createdAt을 LocalDateTime으로 변환
            // BaseEntity의 @CreatedDate와 @LastModifiedDate는 자동으로 처리되므로 여기서 직접 설정할 필요는 없음
        )

        // JPA 리포지토리를 사용해 데이터베이스에 저장
        receiptRepository.save(receiptObject)
    }
}