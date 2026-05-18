package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class EventBroadcastService {

    private static final int MAX_EVENTS_PER_GAME = 500;

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<UUID, List<GameEventMessage>> eventsByGame = new ConcurrentHashMap<>();

    public EventBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(GameEventMessage event) {
        remember(event);
        messagingTemplate.convertAndSend("/topic/games/" + event.gameId() + "/events", event);
    }

    public List<GameEventMessage> eventsSince(UUID gameId, Instant since) {
        List<GameEventMessage> events = eventsByGame.getOrDefault(gameId, List.of());
        return events.stream()
            .filter(event -> since == null || event.occurredAt().isAfter(since))
            .sorted(Comparator.comparing(GameEventMessage::occurredAt))
            .toList();
    }

    private void remember(GameEventMessage event) {
        List<GameEventMessage> events = eventsByGame.computeIfAbsent(event.gameId(), ignored -> new ArrayList<>());
        synchronized (events) {
            events.add(event);
            if (events.size() > MAX_EVENTS_PER_GAME) {
                events.remove(0);
            }
        }
    }
}
