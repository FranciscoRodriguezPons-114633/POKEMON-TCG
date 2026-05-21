package com.utn.pokemontcg.realtime.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import com.utn.pokemontcg.realtime.dto.PendingEventsResponse;
import com.utn.pokemontcg.realtime.infrastructure.persistence.RealtimeEventEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class EventBroadcastService {

    private static final int MAX_EVENTS_PER_GAME = 500;
    private static final int SCHEMA_VERSION = 1;
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {};

    private final SimpMessagingTemplate messagingTemplate;
    private final RealtimeEventStore eventStore;
    private final EventAccessRegistry accessRegistry;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<UUID, List<GameEventEnvelope>> eventsByGame = new ConcurrentHashMap<>();

    public EventBroadcastService(
        SimpMessagingTemplate messagingTemplate,
        RealtimeEventStore eventStore,
        EventAccessRegistry accessRegistry,
        ObjectMapper objectMapper
    ) {
        this.messagingTemplate = messagingTemplate;
        this.eventStore = eventStore;
        this.accessRegistry = accessRegistry;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void rebuildMemoryFromPersistentEvents() {
        eventStore.findAllOrdered().stream()
            .map(this::toEnvelope)
            .forEach(envelope -> {
                remember(envelope);
                accessRegistry.register(envelope);
            });
    }

    public GameEventEnvelope broadcast(GameEventMessage event) {
        GameEventEnvelope envelope = saveAndEnvelope(event);
        remember(envelope);
        accessRegistry.register(envelope);
        messagingTemplate.convertAndSend("/topic/games/" + envelope.gameId() + "/events", envelope);
        return envelope;
    }

    public PendingEventsResponse pendingEvents(UUID gameId, Long sinceSequence, Instant since) {
        List<GameEventEnvelope> events = (sinceSequence == null
                ? eventStore.findByGameId(gameId)
                : eventStore.findByGameIdAfterSequence(gameId, sinceSequence))
            .stream()
            .map(this::toEnvelope)
            .filter(event -> since == null || event.occurredAt().isAfter(since))
            .sorted(Comparator.comparingLong(GameEventEnvelope::sequence))
            .toList();
        long lastSequence = eventStore.findLatestByGameId(gameId)
            .map(RealtimeEventEntity::sequence)
            .orElse(0L);
        return new PendingEventsResponse(gameId, lastSequence, false, Instant.now(), events);
    }

    public List<GameEventEnvelope> eventsSince(UUID gameId, Instant since) {
        return pendingEvents(gameId, null, since).events();
    }

    private GameEventEnvelope saveAndEnvelope(GameEventMessage event) {
        GameEventType type = parseType(event.type());
        Instant receivedAt = Instant.now();
        RealtimeEventEntity saved = eventStore.save(new RealtimeEventEntity(
            SCHEMA_VERSION,
            event.gameId(),
            type,
            payloadJson(event.payload()),
            event.occurredAt(),
            receivedAt
        ));
        return toEnvelope(saved);
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

    private GameEventEnvelope toEnvelope(RealtimeEventEntity entity) {
        return new GameEventEnvelope(
            entity.sequence(),
            entity.schemaVersion(),
            entity.gameId(),
            entity.type(),
            payloadMap(entity.payloadJson()),
            entity.occurredAt(),
            entity.receivedAt()
        );
    }

    private String payloadJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new InvalidGameEventException("No se pudo serializar el payload del evento realtime");
        }
    }

    private Map<String, Object> payloadMap(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, PAYLOAD_TYPE);
        } catch (JsonProcessingException ex) {
            throw new InvalidGameEventException("No se pudo leer el payload persistido del evento realtime");
        }
    }
}
