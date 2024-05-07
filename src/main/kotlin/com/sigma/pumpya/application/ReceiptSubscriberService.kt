package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
                val id: String = messageMap["id"].toString()

                when(topic) {
                    Topic.RECEIPT_CREATED.name -> handleCreateReceipt(id)
                    Topic.RECEIPT_DELETED.name -> handleDeleteReceipt(id)
                }

            } catch (e: Exception) {
                // 메시지 처리 중 오류 발생 시 예외 처리
                println("Error processing message: $e")
            }
        }

    }
    private fun handleCreateReceipt(receiptId: String) {
        val partyId: String = "aaec004b-5137-4996-b5dc-627213c7f648"
        //영수증 정보에 파티 정보가 있으니 그대로 전달하면 됨
        messagingTemplate.convertAndSend("/sub/receipt/$partyId", receiptId)
    }
    private fun handleDeleteReceipt(partyId: String) {
        val partyId: String = "aaec004b-5137-4996-b5dc-627213c7f648"
        //영수증 정보에 파티 정보가 있으니 그대로 전달하면 됨
        messagingTemplate.convertAndSend("/sub/receipt/$partyId/delete", partyId)

    }
}