package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventBroadcastServiceTest {

    @Test
    void shouldRememberEventsForReconnectAndBroadcastToGameTopic() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        EventBroadcastService service = new EventBroadcastService(messagingTemplate);
        UUID gameId = UUID.randomUUID();
        GameEventMessage oldEvent = new GameEventMessage(gameId, "TURN_STARTED", Map.of(), Instant.parse("2026-05-06T18:00:00Z"));
        GameEventMessage newEvent = new GameEventMessage(gameId, "ATTACK_RESOLVED", Map.of("damage", 30), Instant.parse("2026-05-06T18:01:00Z"));

        service.broadcast(oldEvent);
        GameEventEnvelope envelope = service.broadcast(newEvent);

        assertEquals(1, service.eventsSince(gameId, Instant.parse("2026-05-06T18:00:30Z")).size());
        assertEquals(GameEventType.ATTACK_RESOLVED, service.eventsSince(gameId, Instant.parse("2026-05-06T18:00:30Z")).get(0).type());
        assertEquals(2L, envelope.sequence());
        assertEquals(1, envelope.schemaVersion());
        verify(messagingTemplate).convertAndSend("/topic/games/" + gameId + "/events", envelope);
    }

    @Test
    void shouldReturnPendingEventsAfterSequenceForReconnect() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        EventBroadcastService service = new EventBroadcastService(messagingTemplate);
        UUID gameId = UUID.randomUUID();

        service.broadcast(new GameEventMessage(gameId, "TURN_STARTED", Map.of(), Instant.parse("2026-05-06T18:00:00Z")));
        service.broadcast(new GameEventMessage(gameId, "CARD_DRAWN", Map.of("deckRemaining", 46), Instant.parse("2026-05-06T18:01:00Z")));

        var response = service.pendingEvents(gameId, 1L, null);

        assertEquals(2L, response.lastSequence());
        assertEquals(1, response.events().size());
        assertEquals(GameEventType.CARD_DRAWN, response.events().get(0).type());
    }
}
