package com.sigma.pumpya.api.controller

import com.sigma.pumpya.application.CurrencyService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Currency Api")
class CurrencyController (
    private val currencyService : CurrencyService,
){
}