/**
 *
 *  [ Test flow ]
 *
 *
 *
 */

package com.sigma.pumpya.application

import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.repository.ReceiptRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
class PartyServiceTest: BehaviorSpec({

    val receiptService = mockk<ReceiptService>()
    val partyService = PartyService(
        partyRepository = mockk(relaxed = true),
        redisTemplate = mockk(relaxed = true),
        receiptService = receiptService,
        listParser = mockk(relaxed = true),
        objectMapper = mockk(relaxed = true)
    )

    // 영수증 객체 생성
    val mockReceipts = listOf(
        ReceiptDTO("1", "party1", "test1", "name1", 100.00, "USD", "tag", "[]", 0),
        ReceiptDTO("2", "party1", "test2", "name2", 100.00, "USD", "tag", "[]", 1),
        ReceiptDTO("3", "party1", "test3", "name2", 100.00, "USD", "tag", "[name2]", 2),

    )

    given("정산 함수 실행 테스트") {
        every { receiptService.findAllByPartyId("party1") } returns mockReceipts

        `when`("실행") {
            val result = partyService.pumppaya("party1")

            println(result)
        }
    }


})