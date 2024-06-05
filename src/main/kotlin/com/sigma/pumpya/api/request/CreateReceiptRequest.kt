package com.sigma.pumpya.api.request

import java.util.*

data class CreateReceiptRequest(
    val partyId: String,
    val author: String,
    val receiptName: String,
    val cost: Double,
    val useCurrency: String,
    val useTag: String,
    val joins: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        //이 객체는 누구와도 비교하지 않는 클래스이기 때문에 pass
        return super.equals(other)
    }
    override fun hashCode(): Int {
        var result = partyId.hashCode()
        result = 31 * result + partyId.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + receiptName.hashCode()
        result = 31 * result + cost.hashCode()
        result = 31 * result + useCurrency.hashCode()
        result = 32 * result + useTag.hashCode()
        result = 31 * result + joins.hashCode()

        return result
    }
}