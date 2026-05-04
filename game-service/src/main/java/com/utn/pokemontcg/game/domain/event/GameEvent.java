package com.utn.pokemontcg.game.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameEvent(UUID gameId, String type, Map<String, Object> payload, Instant occurredAt) {
    public static GameEvent of(UUID gameId, String type, Map<String, Object> payload) {
        return new GameEvent(gameId, type, payload, Instant.now());
    }
}
