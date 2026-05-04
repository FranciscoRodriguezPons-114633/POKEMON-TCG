package com.utn.pokemontcg.game.infrastructure.repository;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameStateRepository implements GameStateRepository {

    private final Map<UUID, GameAggregate> store = new ConcurrentHashMap<>();

    @Override
    public GameAggregate save(GameAggregate game) {
        store.put(game.id(), game);
        return game;
    }

    @Override
    public Optional<GameAggregate> findById(UUID gameId) {
        return Optional.ofNullable(store.get(gameId));
    }
}
