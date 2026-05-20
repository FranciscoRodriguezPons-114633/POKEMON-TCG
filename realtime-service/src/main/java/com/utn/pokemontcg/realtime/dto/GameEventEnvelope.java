package com.utn.pokemontcg.realtime.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameEventEnvelope(
    long sequence,
    int schemaVersion,
    UUID gameId,
    GameEventType type,
    Map<String, Object> payload,
    Instant occurredAt,
    Instant receivedAt
) {
}
