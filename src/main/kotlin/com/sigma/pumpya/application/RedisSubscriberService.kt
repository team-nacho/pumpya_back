package com.sigma.pumpya.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.pumpya.domain.entity.Receipt
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate

import org.springframework.stereotype.Service

@Service
class RedisSubscriberService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
): MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val channel: String = message.channel.toString()
        val publishMessage: String? = redisTemplate.stringSerializer.deserialize(message.body)
        /**TODO
         * 각 레디스 토픽 채널별 발행된 메세지에 따라 처리
         */
        when (channel) {}
    }
}