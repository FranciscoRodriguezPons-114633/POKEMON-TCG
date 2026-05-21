package com.utn.pokemontcg.realtime.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import com.utn.pokemontcg.realtime.infrastructure.persistence.RealtimeEventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventBroadcastServiceTest {

    @Test
    void shouldRememberEventsForReconnectAndBroadcastToGameTopic() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        EventBroadcastService service = serviceWith(messagingTemplate, new InMemoryRealtimeEventStore());
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
        EventBroadcastService service = serviceWith(messagingTemplate, new InMemoryRealtimeEventStore());
        UUID gameId = UUID.randomUUID();

        service.broadcast(new GameEventMessage(gameId, "TURN_STARTED", Map.of(), Instant.parse("2026-05-06T18:00:00Z")));
        service.broadcast(new GameEventMessage(gameId, "CARD_DRAWN", Map.of("deckRemaining", 46), Instant.parse("2026-05-06T18:01:00Z")));

        var response = service.pendingEvents(gameId, 1L, null);

        assertEquals(2L, response.lastSequence());
        assertFalse(response.replayFromMemory());
        assertEquals(1, response.events().size());
        assertEquals(GameEventType.CARD_DRAWN, response.events().get(0).type());
    }

    private EventBroadcastService serviceWith(SimpMessagingTemplate messagingTemplate, RealtimeEventStore eventStore) {
        return new EventBroadcastService(
            messagingTemplate,
            eventStore,
            new EventAccessRegistry(),
            new ObjectMapper().findAndRegisterModules()
        );
    }

    private static class InMemoryRealtimeEventStore implements RealtimeEventStore {

        private final AtomicLong sequence = new AtomicLong();
        private final List<RealtimeEventEntity> events = new ArrayList<>();

        @Override
        public RealtimeEventEntity save(RealtimeEventEntity event) {
            event.assignSequenceForTest(sequence.incrementAndGet());
            events.add(event);
            return event;
        }

        @Override
        public List<RealtimeEventEntity> findAllOrdered() {
            return events.stream()
                .sorted(Comparator.comparingLong(RealtimeEventEntity::sequence))
                .toList();
        }

        @Override
        public List<RealtimeEventEntity> findByGameId(UUID gameId) {
            return events.stream()
                .filter(event -> event.gameId().equals(gameId))
                .sorted(Comparator.comparingLong(RealtimeEventEntity::sequence))
                .toList();
        }

        @Override
        public List<RealtimeEventEntity> findByGameIdAfterSequence(UUID gameId, long sequence) {
            return events.stream()
                .filter(event -> event.gameId().equals(gameId))
                .filter(event -> event.sequence() > sequence)
                .sorted(Comparator.comparingLong(RealtimeEventEntity::sequence))
                .toList();
        }

        @Override
        public Optional<RealtimeEventEntity> findLatestByGameId(UUID gameId) {
            return events.stream()
                .filter(event -> event.gameId().equals(gameId))
                .max(Comparator.comparingLong(RealtimeEventEntity::sequence));
        }
    }
}
