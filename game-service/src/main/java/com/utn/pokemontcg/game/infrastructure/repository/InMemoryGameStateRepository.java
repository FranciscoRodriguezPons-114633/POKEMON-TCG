package com.utn.pokemontcg.game.infrastructure.repository;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class InMemoryGameStateRepository implements GameStateRepository {

    private final Map<UUID, GameAggregate> store = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> logs = new ConcurrentHashMap<>();

    @Override
    public GameAggregate save(GameAggregate game) {
        store.put(game.id(), game);
        return game;
    }

    @Override
    public Optional<GameAggregate> findById(UUID gameId) {
        return Optional.ofNullable(store.get(gameId));
    }

    @Override
    public void appendActionLog(UUID gameId, GameActionType action, UUID playerId, String result, Instant timestamp) {
        logs.computeIfAbsent(gameId, id -> new ArrayList<>())
            .add(timestamp + " | player=" + playerId + " | action=" + action + " | result=" + result);
    }

    @Override
    public List<String> actionLog(UUID gameId) {
        return List.copyOf(logs.getOrDefault(gameId, List.of()));
    }

    @Override
    public Optional<GameAggregate> latestSnapshot(UUID gameId) {
        return findById(gameId);
    }
}
