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
        //해당 파티의 총액을 레디스에 업데이트, 발행인의 총 사용 금액 업데이트
        //해당 파티의 계산 결과 업데이트
        //메세지에 최신화된 파티 총 금액 전달
        val publishMessage: String? = redisTemplate.stringSerializer.deserialize(message.body)
        val receipt: Receipt = objectMapper.readValue(publishMessage, Receipt::class.java)
        messagingTemplate.convertAndSend("/sub/channels/" + receipt.partyId, publishMessage!!)
    }
}