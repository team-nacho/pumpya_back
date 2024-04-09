package com.sigma.pumpya.infrastructure

import java.time.ZoneId
import java.time.ZonedDateTime

object EpochTimeUtil {
    private const val TIME_ZONE = "Asia/Seoul"

    fun getCurrentEpochTime() = ZonedDateTime.now(ZoneId.of(TIME_ZONE))
        .toInstant()
        .toEpochMilli()

    fun convertEpochTimeToLocalDateTime(epochTime: Long): ZonedDateTime = ZonedDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(epochTime),
        ZoneId.of(TIME_ZONE)
    )
}