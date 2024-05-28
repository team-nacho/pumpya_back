package com.sigma.pumpya.infrastructure.enums

enum class Channel(val topic: String) {
    RECEIPT("receiptChannel"),
    MEMBER("memberChannel"),
    PARTY_END("partyEndChannel")
}