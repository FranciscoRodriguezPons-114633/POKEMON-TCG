package com.utn.pokemontcg.realtime.config;

import com.utn.pokemontcg.realtime.application.EventAccessRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final EventAccessRegistry accessRegistry;

    public WebSocketConfig(EventAccessRegistry accessRegistry) {
        this.accessRegistry = accessRegistry;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                    validateGameTopicSubscription(accessor);
                }
                return message;
            }
        });
    }

    private void validateGameTopicSubscription(StompHeaderAccessor accessor) {
        UUID gameId = gameIdFrom(accessor.getDestination());
        if (gameId == null) {
            return;
        }

        UUID playerId = playerIdFrom(accessor.getFirstNativeHeader("playerId"));
        if (playerId == null || !accessRegistry.canSubscribe(gameId, playerId)) {
            throw new IllegalArgumentException("El jugador no puede suscribirse a esta partida");
        }
    }

    private UUID gameIdFrom(String destination) {
        if (destination == null || !destination.startsWith("/topic/games/") || !destination.endsWith("/events")) {
            return null;
        }
        String rawGameId = destination
            .replace("/topic/games/", "")
            .replace("/events", "");
        return UUID.fromString(rawGameId);
    }

    private UUID playerIdFrom(String rawPlayerId) {
        if (rawPlayerId == null || rawPlayerId.isBlank()) {
            return null;
        }
        return UUID.fromString(rawPlayerId);
    }
}
