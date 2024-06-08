package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.domain.entity.Receipt
import com.sigma.pumpya.infrastructure.dto.ReceiptDTO
import com.sigma.pumpya.infrastructure.enums.Topic
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class ReceiptSubscriberService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
): MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val publishMessage: String? = redisTemplate.stringSerializer.deserialize(message.body)
        publishMessage?.let { messageString ->
            try {
                val mapType = object : TypeReference<Map<String, Any>>() {}
                val messageMap: Map<String, Any> = objectMapper.readValue(messageString, mapType)

                val topic: String = messageMap["topic"].toString()
                val receipt: ReceiptDTO = objectMapper.readValue(messageMap["receipt"].toString(), ReceiptDTO::class.java)
                when(topic) {
                    Topic.RECEIPT_CREATED.name -> handleCreateReceipt(receipt)
                    Topic.RECEIPT_DELETED.name -> handleDeleteReceipt(receipt)
                }

            } catch (e: Exception) {
                // 메시지 처리 중 오류 발생 시 예외 처리
                println("Error processing message: $e")
            }
        }

    }
    private fun handleCreateReceipt(receipt: ReceiptDTO) {

        messagingTemplate.convertAndSend("/sub/receipt/${receipt.partyId}", objectMapper.writeValueAsString(receipt))
    }
    private fun handleDeleteReceipt(receipt: ReceiptDTO) {
        messagingTemplate.convertAndSend("/sub/receipt/${receipt.partyId}/delete", receipt.receiptId)

    }
}