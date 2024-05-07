package com.sigma.pumpya.api.request

import java.util.*

data class CreateReceiptRequest(
    val partyId: String,
    val name: String,
    val cost: Double,
    val join: Array<String>,
    val currency: String,
    val createdAt: Date,
) {
    override fun equals(other: Any?): Boolean {
        //이 객체는 누구와도 비교하지 않는 클래스이기 때문에 pass
        return super.equals(other)
    }
    override fun hashCode(): Int {
        var result = partyId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + cost.hashCode()
        result = 31 * result + join.contentHashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}