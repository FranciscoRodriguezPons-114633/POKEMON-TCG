package com.utn.pokemontcg.realtime.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PendingEventsResponse(
    UUID gameId,
    long lastSequence,
    boolean replayFromMemory,
    Instant syncedAt,
    List<GameEventEnvelope> events
) {
}
