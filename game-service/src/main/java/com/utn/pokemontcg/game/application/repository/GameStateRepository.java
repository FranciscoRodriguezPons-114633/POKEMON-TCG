package com.utn.pokemontcg.game.application.repository;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

import java.util.Optional;
import java.util.UUID;

public interface GameStateRepository {
    GameAggregate save(GameAggregate game);
    Optional<GameAggregate> findById(UUID gameId);
}
