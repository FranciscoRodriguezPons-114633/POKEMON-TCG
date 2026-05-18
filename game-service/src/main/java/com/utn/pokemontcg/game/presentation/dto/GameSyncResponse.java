package com.utn.pokemontcg.game.presentation.dto;

import java.time.Instant;
import java.util.List;

public record GameSyncResponse(
    GameStateResponse state,
    List<String> actionLog,
    Instant syncedAt
) {
}
