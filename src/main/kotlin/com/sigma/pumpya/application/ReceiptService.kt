package com.sigma.pumpya.application

import com.sigma.pumpya.api.request.CreateReceiptRequest
import com.sigma.pumpya.domain.entity.Party
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.repository.PartyRepository
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.util.*
@Service
class ReceiptService (
    private val receiptRepository: ReceiptRepository,
    private val partyRepository: PartyRepository
){
    fun saveReceipt(createReceiptRequest: CreateReceiptRequest) {
        val joins = createReceiptRequest.joins.joinToString(",")

        // CreateReceiptRequest에서 받은 정보를 Receipt 엔티티 객체로 변환
        val receiptObject = Receipt(
            receiptId = UUID.randomUUID().toString(),
            partyId = createReceiptRequest.partyId,
            author = createReceiptRequest.author,
            receiptName = createReceiptRequest.receiptName,
            cost = createReceiptRequest.cost,
            useCurrency = createReceiptRequest.useCurrency,
            useTag = "", // useTag가 CreateReceiptRequest에 없으므로 빈 문자열 또는 기본값을 설정
            joins = joins, // Array<String>을 콤마로 구분된 String으로 변환
            // createdAt을 LocalDateTime으로 변환
            // BaseEntity의 @CreatedDate와 @LastModifiedDate는 자동으로 처리되므로 여기서 직접 설정할 필요는 없음
        )

        // JPA 리포지토리를 사용해 데이터베이스에 저장
        receiptRepository.save(receiptObject)
    }

    fun getReceiptsByPartyId(partyId : String): List<ReceiptDTO> {
        val party = partyRepository.findByPartyId(partyId)
        if(party == null) {
            throw NotFoundException()
        } else return findAllByPartyId(partyId)
    }
    fun deleteReceipt(receiptId: String) : String{
        //DB에서 삭제
        //TODO 만약 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
        val receipt = receiptRepository.findById(receiptId)
        if (receipt.isPresent) {
            val partyId = receipt.get().partyId
            val useCurrency = receipt.get().useCurrency

            // 해당 통화에 대한 다른 영수증이 있는지 확인
            val otherReceipts = findAllByUseCurrency(useCurrency)
            if (otherReceipts.isEmpty()) {
                // 해당 통화에 대한 기록이 전부 삭제되었다면 파티 내역에서 삭제
                val partyObject = partyRepository.findById(partyId)
                val useCurrencies = partyObject.get().usedCurrencies

                if( useCurrencies.contains(useCurrency) ) {
                    val currencyList = useCurrencies.split(",").filter { currencyPair ->
                        val (currency, _) = currencyPair.split(":")
                        currency != useCurrency
                    }.joinToString(",")

                    val updatedParty = Party(
                        partyId = partyObject.get().partyId,
                        partyName = partyObject.get().partyName,
                        totalCost = partyObject.get().totalCost,
                        usedCurrencies = currencyList
                    )
                    partyRepository.save(updatedParty)
                }
            }
            // 영수증 삭제
            receiptRepository.deleteById(receiptId)
            return "success"
        } else {
            return "fail"
        }
    }

    /**
     * TODO
     *  아마 이 부분은 repository의 책임인 것 같음
     */


    fun findAllByUseCurrency(useCurrency: String) : List<ReceiptDTO>{
        val receiptList = receiptRepository.findAll()
        val result = mutableListOf<ReceiptDTO>()
        for(receipt in receiptList) {
            if(receipt.useCurrency == useCurrency) {
                result.add(receipt.toDTO())
            }
        }
        if(receiptList.isEmpty()) return mutableListOf<ReceiptDTO>() //Error Emerge
        return result
    }
    fun findAllByPartyId(partyId: String): List<ReceiptDTO> {
        val receiptList = receiptRepository.findAll()
        val result = mutableListOf<ReceiptDTO>()
        for(receipt in receiptList) {
            if(receipt.partyId == partyId) {
                result.add(receipt.toDTO())
            }
        }
        if(receiptList.isEmpty()) return mutableListOf<ReceiptDTO>() //Error Emerge
        return result
    }
}