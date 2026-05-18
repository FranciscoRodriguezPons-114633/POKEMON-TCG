package com.utn.pokemontcg.game.presentation.dto;

import java.util.Map;
import java.util.UUID;

public record GameStateResponse(
    UUID gameId,
    String state,
    String phase,
    UUID currentTurnPlayer,
    UUID firstPlayer,
    UUID winner,
    Map<UUID, ?> setupByPlayer,
    Map<UUID, ?> activePokemonByPlayer,
    Map<UUID, ?> benchByPlayer,
    Map<UUID, ?> handSizesByPlayer,
    Map<UUID, ?> deckCardsRemainingByPlayer,
    Map<UUID, ?> prizeCardsRemainingByPlayer,
    Map<UUID, ?> discardPilesByPlayer,
    Map<UUID, ?> activeDamageCountersByPlayer,
    Map<UUID, ?> activeAttachedEnergyByPlayer,
    Map<UUID, ?> statusByPlayer
) {
}
