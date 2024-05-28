package com.sigma.pumpya.application

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.mockk
import org.springframework.data.redis.core.RedisTemplate

class PartyServiceTest: BehaviorSpec({

    val redisTemplate  = mockk<RedisTemplate<String, String>>()
    val redisPublisherService = mockk<RedisPublisherService>()
    val partyService = PartyService(redisTemplate, redisPublisherService)

    afterTest {

    }
    given("create party test") {
    }
})