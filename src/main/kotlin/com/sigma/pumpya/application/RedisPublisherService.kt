package com.sigma.pumpya.application

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisPublisherService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun publishMessage(message: String, channel: String) {
        redisTemplate.convertAndSend(channel, message)
    }
}