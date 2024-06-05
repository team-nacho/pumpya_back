package com.sigma.pumpya.application

import com.sigma.pumpya.api.request.CreateCurrencyRequest
import com.sigma.pumpya.domain.entity.Currency
import com.sigma.pumpya.infrastructure.repository.CurrencyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CurrencyService(
    private val currencyRepository: CurrencyRepository
) {
    fun createCurrency(createCurrencyRequest: CreateCurrencyRequest): Currency {
        val res =
            currencyRepository.save(
                Currency(createCurrencyRequest.currencyId, createCurrencyRequest.currencyName)
            )

        return res
    }

    fun getCurrencies(): List<Currency> {
        val res = currencyRepository.findAll() ?: emptyList()
        return res
    }

    @Transactional
    fun deleteCurrency(currencyId: String) {
        return currencyRepository.deleteById(currencyId)
    }

}