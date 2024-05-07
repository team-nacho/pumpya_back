package com.sigma.pumpya.infrastructure.config
import com.sigma.pumpya.application.MemberSubscriberService
import com.sigma.pumpya.application.PartySubscriberService
import com.sigma.pumpya.application.ReceiptSubscriberService
import com.sigma.pumpya.infrastructure.enums.Channel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    val host: String,

    @Value("\${spring.data.redis.port}")
    val port: Int,

    @Value("\${spring.data.redis.password}")
    val password: String,

    @Value("\${spring.data.redis.name}")
    val name: String,
) {
    @Bean
    fun redisMessageListenerContainer( // (1)
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: List<MessageListenerAdapter>,
        channelTopics: List<ChannelTopic>
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)

        listenerAdapter.forEachIndexed{ index, adapter ->
            container.addMessageListener(adapter, channelTopics[index])
        }
        return container
    }

    @Bean
    fun receiptListenerAdapter(receiptSubscriber: ReceiptSubscriberService): MessageListenerAdapter {
        return MessageListenerAdapter(receiptSubscriber, "onReceiptMessage")
    }

    @Bean
    fun memberListenerAdapter(memberSubscriber: MemberSubscriberService): MessageListenerAdapter {
        return MessageListenerAdapter(memberSubscriber, "onMemberMessage")
    }

    @Bean
    fun partyListenerAdapter(partySubscriber: PartySubscriberService): MessageListenerAdapter {
        return MessageListenerAdapter(partySubscriber, "onPartyEndMessage")
    }
    @Bean
    fun receiptChannelTopic(): ChannelTopic {
        return ChannelTopic(Channel.RECEIPT.topic)
    }

    @Bean
    fun memberChannelTopic(): ChannelTopic {
        return ChannelTopic(Channel.MEMBER.topic)
    }

    @Bean
    fun partyEndChannelTopic(): ChannelTopic {
        return ChannelTopic(Channel.PARTY_END.topic)
    }
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfiguration = RedisStandaloneConfiguration()
        redisConfiguration.setPassword(password)
        redisConfiguration.port = port
        redisConfiguration.username = name
        redisConfiguration.hostName = host
        return LettuceConnectionFactory(redisConfiguration)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory?): RedisTemplate<*, *>? {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = connectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = Jackson2JsonRedisSerializer(String::class.java)
        return redisTemplate
    }
}