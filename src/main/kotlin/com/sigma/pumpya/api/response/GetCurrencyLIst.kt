package com.sigma.pumpya.api.response

import com.sigma.pumpya.domain.entity.Currency

data class GetCurrencyLIst(
    val currencies: List<Currency>
)
