package com.utn.pokemontcg.presentation.dto.websocket;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GameActionMessage(
    @NotNull UUID playerId,
    @NotNull GameActionType type
) {
}
