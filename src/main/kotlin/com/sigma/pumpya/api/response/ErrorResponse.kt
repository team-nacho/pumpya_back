package com.sigma.pumpya.api.response

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    val createdAt : LocalDateTime,
    val status: Int,
    val message: String
) {
    constructor(httpStatus : HttpStatus, message: String) : this(
        createdAt = LocalDateTime.now(),
        status = httpStatus.value(),
        message = message
    )
}
