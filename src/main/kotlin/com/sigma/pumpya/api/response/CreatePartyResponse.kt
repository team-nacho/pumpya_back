package com.sigma.pumpya.api.response

import com.sigma.pumpya.infrastructure.dto.PartyDTO
import java.util.*

data class CreatePartyResponse(
    val partyId: String,
    val partyName: String,
    val usedCurrencies: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePartyResponse

        if (partyId != other.partyId) return false
        if (partyName != other.partyName) return false
        if (!usedCurrencies.contentEquals(other.usedCurrencies)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = partyId.hashCode()
        result = 31 * result + partyName.hashCode()
        result = 31 * result + usedCurrencies.contentHashCode()
        return result
    }
}