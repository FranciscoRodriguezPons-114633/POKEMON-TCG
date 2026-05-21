package com.utn.pokemontcg.realtime.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.game.domain.event.GameEvent;
import com.utn.pokemontcg.game.infrastructure.repository.HttpRealtimeObserver;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameServiceRealtimeFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldSendGameServiceObserverEventToRealtimeWebSocketSubscribers() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        HttpRealtimeObserver gameServiceObserver = new HttpRealtimeObserver("http://localhost:" + port);
        ArrayBlockingQueue<GameEventEnvelope> received = new ArrayBlockingQueue<>(1);

        gameServiceObserver.onEvent(GameEvent.of(
            gameId,
            "GAME_CREATED",
            Map.of("playerOne", playerId.toString())
        ));

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(new ObjectMapper().findAndRegisterModules());
        stompClient.setMessageConverter(messageConverter);

        StompSession session = stompClient
            .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
            .get(3, TimeUnit.SECONDS);

        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/topic/games/" + gameId + "/events");
        subscribeHeaders.add("playerId", playerId.toString());
        session.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameEventEnvelope.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.offer((GameEventEnvelope) payload);
            }
        });
        Thread.sleep(300);

        gameServiceObserver.onEvent(GameEvent.of(
            gameId,
            "ENERGY_ATTACHED",
            Map.of("player", playerId.toString(), "attachedEnergy", 1)
        ));

        GameEventEnvelope event = received.poll(3, TimeUnit.SECONDS);

        assertNotNull(event);
        assertEquals(gameId, event.gameId());
        assertEquals(GameEventType.ENERGY_ATTACHED, event.type());
        assertEquals(1, event.payload().get("attachedEnergy"));

        session.disconnect();
        stompClient.stop();
    }
}

