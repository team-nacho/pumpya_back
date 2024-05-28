package com.sigma.pumpya.api.request

import java.util.*

data class CreateReceiptRequest(
    val partyId: String,
    val receiptName: String,
    val cost: Double,
    val joins: Array<String>,
    val useCurrency: String,
    val createdAt: Date,
    val tag: String,
    val author: String,
) {
    override fun equals(other: Any?): Boolean {
        //이 객체는 누구와도 비교하지 않는 클래스이기 때문에 pass
        return super.equals(other)
    }
    override fun hashCode(): Int {
        var result = partyId.hashCode()
        result = 31 * result + partyId.hashCode()
        result = 31 * result + receiptName.hashCode()
        result = 31 * result + cost.hashCode()
        result = 31 * result + joins.hashCode()
        result = 31 * result + useCurrency.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 32 * result + tag.hashCode()

        return result
    }
}