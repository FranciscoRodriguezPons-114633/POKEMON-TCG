package com.utn.pokemontcg.realtime.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RealtimeWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldBroadcastPublishedEventsToStompSubscribers() throws Exception {
        UUID gameId = UUID.randomUUID();
        ArrayBlockingQueue<GameEventEnvelope> received = new ArrayBlockingQueue<>(1);
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(new ObjectMapper().findAndRegisterModules());
        stompClient.setMessageConverter(messageConverter);

        StompSession session = stompClient
            .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
            .get(3, TimeUnit.SECONDS);

        session.subscribe("/topic/games/" + gameId + "/events", new StompFrameHandler() {
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

        ResponseEntity<GameEventEnvelope> response = restTemplate.postForEntity(
            "/internal/events",
            new GameEventMessage(gameId, "ENERGY_ATTACHED", Map.of("attachedEnergy", 1), Instant.parse("2026-05-06T18:00:00Z")),
            GameEventEnvelope.class
        );

        GameEventEnvelope event = received.poll(3, TimeUnit.SECONDS);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(event);
        assertEquals(gameId, event.gameId());
        assertEquals(GameEventType.ENERGY_ATTACHED, event.type());
        assertEquals(1, event.schemaVersion());
        session.disconnect();
        stompClient.stop();
    }
}
