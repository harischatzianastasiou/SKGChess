package com.chess.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println("Configuring message broker...");
        config.enableSimpleBroker("/topic");        //Sets up a message broker prefix /topic for server-to-client messages
        config.setApplicationDestinationPrefixes("/app");      //Sets /app as the prefix for client-to-server messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("Registering STOMP endpoints...");
        registry.addEndpoint("/chess-websocket")
               .setAllowedOriginPatterns("http://localhost:[*]")  // For development. In production, specify your domain
               .withSockJS();
    }
}