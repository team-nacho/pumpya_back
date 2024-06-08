package com.sigma.pumpya.api.controller.exception.handler

import com.sigma.pumpya.api.controller.exception.*
import com.sigma.pumpya.api.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PumpApiExceptionHandler {
    //PartyId Do not Exist
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PartyIdNotFoundException::class)
    fun handlePartyIdNotFoundException(exception: PartyIdNotFoundException)
        : ErrorResponse {
        return ErrorResponse(HttpStatus.NOT_FOUND, exception.message!!)
    }

    //Tag Do not Exist
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TagNotFoundException::class)
    fun handleTagNotFoundException(exception: TagNotFoundException)
    : ErrorResponse {
        return ErrorResponse(HttpStatus.BAD_REQUEST, exception.message!!)
    }

    //Currency Do not Exist
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CurrencyNotFoundException::class)
    fun handlecurrencyNotFoundException(exception: CurrencyNotFoundException)
            : ErrorResponse {
        return ErrorResponse(HttpStatus.BAD_REQUEST, exception.message!!)
    }
    //ReceiptId Do not Exist
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReceiptNotFoundException::class)
    fun handleReceiptyNotFoundException(exception: TagNotFoundException)
            : ErrorResponse {
        return ErrorResponse(HttpStatus.NOT_FOUND, exception.message!!)
    }
    //MemberName Do not Exist
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MemberNameNotFoundException::class)
    fun handleMemberNameNotFoundException(exception:MemberNameNotFoundException)
    : ErrorResponse {
        return ErrorResponse(HttpStatus.BAD_REQUEST, exception.message!!)
    }
}