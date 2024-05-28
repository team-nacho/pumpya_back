package com.sigma.pumpya.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig: WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) { // (2)
        registry.enableSimpleBroker("/sub") // (3)
        registry.setApplicationDestinationPrefixes("/pub") // (4)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) { // (5)
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
    }
}