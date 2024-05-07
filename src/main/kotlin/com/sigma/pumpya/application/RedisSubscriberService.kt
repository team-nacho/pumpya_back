package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.domain.entity.Receipt
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate

import org.springframework.stereotype.Service

enum class Channel(val topic: String) {
    RECEIPT("receiptChannel"),
    MEMBER("memberChannel"),
    PARTY_END("partyEndChannel")
    // 다른 채널 추가 가능
}

enum class Topic {
    RECEIPT_CREATED,
    RECEIPT_DELETED,
    MEMBER_REGISTERED,
    PARTY_ENDED
    // 다른 토픽 추가 가능
}
@Service
class RedisSubscriberService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
): MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val channel: String = message.channel.toString()
        val publishMessage: String? = redisTemplate.stringSerializer.deserialize(message.body)

        when (channel) {
            Channel.RECEIPT.topic -> handleCreateReceiptMessage(publishMessage)
        }
    }

    private fun handleCreateReceiptMessage(message: String?) {
        message?.let { messageString ->
            try {
                val mapType = object : TypeReference<Map<String, Any>>() {}
                val messageMap: Map<String, Any> = objectMapper.readValue(messageString, mapType)

                val topic: String = messageMap["topic"].toString()
                val id: String = messageMap["id"].toString()

                // 여기에서 topic 및 receiptId를 기반으로 필요한 작업을 수행합니다.
                when(topic) {
                    Topic.RECEIPT_CREATED.name -> handleCreateReceipt(id)
                }

            } catch (e: Exception) {
                // 메시지 처리 중 오류 발생 시 예외 처리
                println("Error processing message: $e")
            }
        }

    }

    private fun handleCreateReceipt(receiptId: String) {
    }
}