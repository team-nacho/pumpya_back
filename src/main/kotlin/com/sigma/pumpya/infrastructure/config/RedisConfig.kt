package com.sigma.pumpya.infrastructure.config
import com.sigma.pumpya.application.RedisSubscriberService
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
        listenerAdapter: MessageListenerAdapter,
        channelTopic: ChannelTopic
    ): RedisMessageListenerContainer {
        /*
        * Redis Channel에서 메세지를 받고 주입된 리스너들에게
        * 비동기적으로 디스패치하는 역할을 하는 컨테이너]
        *  발행된 메세지 처리를 위한 리스너들을 설정
        * */
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(listenerAdapter, channelTopic)
        return container
    }

    @Bean
    fun listenerAdapter(subscriber: RedisSubscriberService): MessageListenerAdapter { // (2)
        /*
        * RedisMessageListenerContainer로부터 메세지를 디스패치 받고
        * 실제 메세지를 처리하는 비즈니스 로직*/

        return MessageListenerAdapter(subscriber, "onMessage")
    }
    @Bean
    fun channelTopic(): ChannelTopic { // (4)
        return ChannelTopic("chatroom")
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