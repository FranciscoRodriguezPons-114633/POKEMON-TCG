package com.utn.pokemontcg.presentation.dto.response;

import com.utn.pokemontcg.domain.model.Game;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameStateResponse(
    UUID gameId,
    String state,
    String phase,
    UUID currentTurnPlayer,
    UUID winnerId,
    List<UUID> players,
    Instant createdAt
) {
    public static GameStateResponse from(Game game) {
        return new GameStateResponse(
            game.getId(),
            game.getState().name(),
            game.getPhase().name(),
            game.getCurrentTurnPlayer(),
            game.getWinnerId(),
            game.getPlayers().stream().map(p -> p.playerId()).toList(),
            game.getCreatedAt()
        );
    }
}
