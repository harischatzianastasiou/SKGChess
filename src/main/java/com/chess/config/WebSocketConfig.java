package com.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.chess.model.session.SessionManager;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private SessionManager sessionManager;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { // ets up an in-memory message broker with one or more destinations for sending and receiving messages. The destination prefix /topic is used for messages to be carried to all subscribed clients via the pub-sub model.
        config.enableSimpleBroker("/topic");  
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chess-websocket")
                .setAllowedOriginPatterns("http://localhost:[*]")  // For development. In production, specify your domain
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    logger.debug("Processing message - command: {}, sessionId: {}, destination: {}", 
                        accessor.getCommand(), accessor.getSessionId(), accessor.getDestination());

                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String sessionId = accessor.getSessionId();
                        logger.info("Client connecting - sessionId: {}", sessionId);
                        sessionManager.createSession(sessionId, null);
                    } 
                    else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String sessionId = accessor.getSessionId();
                        String destination = accessor.getDestination();
                        logger.info("Client subscribing - sessionId: {}, destination: {}", sessionId, destination);
                    }
                    else if (StompCommand.SEND.equals(accessor.getCommand())) {
                        String sessionId = accessor.getSessionId();
                        String destination = accessor.getDestination();
                        logger.info("Client sending message - sessionId: {}, destination: {}", sessionId, destination);
                    }
                    else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        String sessionId = accessor.getSessionId();
                        logger.info("Client disconnecting - sessionId: {}", sessionId);
                        sessionManager.removeSession(sessionId);
                    }
                }
                
                return message;
            }
        });
    }
}