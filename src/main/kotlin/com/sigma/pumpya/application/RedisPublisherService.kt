package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisPublisherService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun publishReceiptMessage(receiptId: String, topic: String, receipt: String) {
        println("publishReceiptMessage:$receiptId")
        val messageMap = mapOf("topic" to topic, "id" to receiptId, "receipt" to receipt)
        val jsonMessage = objectMapper.writeValueAsString(messageMap)
        redisTemplate.convertAndSend("receiptChannel", jsonMessage)
    }

    fun publishMemberMessage(partyId: String, topic: String, name: String) {
        println("publishMemberMessage:$partyId")
        val messageMap = mapOf("topic" to topic, "id" to partyId, "name" to name)
        val jsonMessage = objectMapper.writeValueAsString(messageMap)
        redisTemplate.convertAndSend("memberChannel", jsonMessage)
    }
    fun publishPartyMessage(partyId: String, topic: String) {
        println("publishPartyMessage:$partyId")
        val messageMap = mapOf("topic" to topic, "id" to partyId)
        val jsonMessage = objectMapper.writeValueAsString(messageMap)
        redisTemplate.convertAndSend("partyEndChannel", jsonMessage)
    }
}