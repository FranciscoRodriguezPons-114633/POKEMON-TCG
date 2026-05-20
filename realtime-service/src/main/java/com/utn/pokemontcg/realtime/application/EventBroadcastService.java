package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import com.utn.pokemontcg.realtime.dto.PendingEventsResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EventBroadcastService {

    private static final int MAX_EVENTS_PER_GAME = 500;
    private static final int SCHEMA_VERSION = 1;

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<UUID, List<GameEventEnvelope>> eventsByGame = new ConcurrentHashMap<>();

    public EventBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public GameEventEnvelope broadcast(GameEventMessage event) {
        GameEventEnvelope envelope = envelope(event);
        remember(envelope);
        messagingTemplate.convertAndSend("/topic/games/" + envelope.gameId() + "/events", envelope);
        return envelope;
    }

    public PendingEventsResponse pendingEvents(UUID gameId, Long sinceSequence, Instant since) {
        List<GameEventEnvelope> events = eventsByGame.getOrDefault(gameId, List.of()).stream()
            .filter(event -> sinceSequence == null || event.sequence() > sinceSequence)
            .filter(event -> since == null || event.occurredAt().isAfter(since))
            .sorted(Comparator.comparingLong(GameEventEnvelope::sequence))
            .toList();
        long lastSequence = eventsByGame.getOrDefault(gameId, List.of()).stream()
            .mapToLong(GameEventEnvelope::sequence)
            .max()
            .orElse(0L);
        return new PendingEventsResponse(gameId, lastSequence, true, Instant.now(), events);
    }

    public List<GameEventEnvelope> eventsSince(UUID gameId, Instant since) {
        return pendingEvents(gameId, null, since).events();
    }

    private GameEventEnvelope envelope(GameEventMessage event) {
        GameEventType type = parseType(event.type());
        return new GameEventEnvelope(
            sequence.incrementAndGet(),
            SCHEMA_VERSION,
            event.gameId(),
            type,
            event.payload(),
            event.occurredAt(),
            Instant.now()
        );
    }

    private GameEventType parseType(String rawType) {
        try {
            return GameEventType.valueOf(rawType);
        } catch (IllegalArgumentException ex) {
            throw new InvalidGameEventException("Tipo de evento no soportado: " + rawType);
        }
    }

    private void remember(GameEventEnvelope event) {
        List<GameEventEnvelope> events = eventsByGame.computeIfAbsent(event.gameId(), ignored -> new ArrayList<>());
        synchronized (events) {
            events.add(event);
            if (events.size() > MAX_EVENTS_PER_GAME) {
                events.remove(0);
            }
        }
    }
}
