package com.utn.pokemontcg.game.presentation.dto;

import java.util.Map;
import java.util.UUID;

public record GameStateResponse(
    UUID gameId,
    String state,
    String phase,
    UUID currentTurnPlayer,
    UUID firstPlayer,
    Map<UUID, ?> setupByPlayer
) {
}
