package com.sigma.pumpya.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class PartySubscriberService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
): MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {

        val channel: String = message.channel.toString()
        val publishMessage: String? = redisTemplate.stringSerializer.deserialize(message.body)
        println(channel)
        publishMessage?.let { messageString ->
            try {
                val mapType = object : TypeReference<Map<String, Any>>() {}
                val messageMap: Map<String, Any> = objectMapper.readValue(messageString, mapType)

                val topic: String = messageMap["topic"].toString()
                val id: String = messageMap["id"].toString()

                println("handleCreateReceipt:$id")
                val partyId: String = "aaec004b-5137-4996-b5dc-627213c7f648"
                //영수증 정보에 파티 정보가 있으니 그대로 전달하면 됨
                messagingTemplate.convertAndSend("/sub/$partyId/end", id)
            } catch (e: Exception) {
                // 메시지 처리 중 오류 발생 시 예외 처리
                println("Error processing message: $e")
            }
        }

    }
}