package com.utn.pokemontcg.realtime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameEventMessage(
    @NotNull UUID gameId,
    @NotBlank String type,
    @NotNull Map<String, Object> payload,
    @NotNull Instant occurredAt
) {
}
