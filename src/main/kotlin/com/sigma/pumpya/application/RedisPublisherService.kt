package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisPublisherService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    /**TODO
     * 들어온 메세지를 직렬화해서 레디스에 발행
     */
    fun publishReceiptMessage(receiptId: String) {
        val messageMap = mapOf("topic" to "create_receipt", "id" to receiptId)
        val jsonMessage = objectMapper.writeValueAsString(messageMap)
        redisTemplate.convertAndSend("receiptChannel", jsonMessage)
    }
}