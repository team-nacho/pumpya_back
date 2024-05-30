package com.sigma.pumpya.api.controller

import com.sigma.pumpya.api.request.CreateCurrencyRequest
import com.sigma.pumpya.api.response.CreateCurrencyResponse
import com.sigma.pumpya.application.CurrencyService
import com.sigma.pumpya.application.GetCurrencyLIst
import com.sigma.pumpya.domain.entity.Currency
import com.sigma.pumpya.infrastructure.repository.CurrencyRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Currency Api")
@RestController("/currency")
class CurrencyController (
    private val currencyRepository: CurrencyRepository
){
    @Operation(summary = "create currency")
    @PostMapping("/create-currency")
    @Transactional
    fun createCurrency(
        @Valid @RequestBody createCurrencyRequest: CreateCurrencyRequest
    ): CreateCurrencyResponse {
        val res =
            currencyRepository.save(
                Currency(createCurrencyRequest.currencyId, createCurrencyRequest.currencyName)
            )
        return CreateCurrencyResponse(res.currencyId, res.country)
    }

    @Operation(summary = "get currency list")
    @GetMapping("/get-currencies")
    @Transactional
    fun getCurrencies():  GetCurrencyLIst {
        val res = currencyRepository.findAll() ?: emptyList()
        return GetCurrencyLIst(res)
    }

    /*
        TODO
            반환형을 정의 안함ㅋ
     */
    @Operation(summary = "delete currency with currency id")
    @PostMapping("/delete-currency/{currencyId}")
    @Transactional
    fun deleteCurrencies(
        @PathVariable currencyId: String
    ) {
        return currencyRepository.deleteById(currencyId)
    }

}