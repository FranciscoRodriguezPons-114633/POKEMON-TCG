package com.utn.pokemontcg.game.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!memory")
@Transactional
public class JpaGameStateRepository implements GameStateRepository {

    private final GameSnapshotJpaRepository snapshots;
    private final GameActionLogJpaRepository logs;
    private final ObjectMapper objectMapper;

    public JpaGameStateRepository(GameSnapshotJpaRepository snapshots,
                                  GameActionLogJpaRepository logs,
                                  ObjectMapper objectMapper) {
        this.snapshots = snapshots;
        this.logs = logs;
        this.objectMapper = objectMapper;
    }

    @Override
    public GameAggregate save(GameAggregate game) {
        GameSnapshotEntity entity = new GameSnapshotEntity();
        entity.setGameId(game.id());
        entity.setVersion(snapshots.countByGameId(game.id()) + 1);
        entity.setPayload(serialize(game));
        entity.setUpdatedAt(Instant.now());
        snapshots.save(entity);
        return game;
    }

    @Override
    public Optional<GameAggregate> findById(UUID gameId) {
        return latestSnapshot(gameId);
    }

    @Override
    public void appendActionLog(UUID gameId, GameActionType action, UUID playerId, String result, Instant timestamp) {
        GameActionLogEntity log = new GameActionLogEntity();
        log.setGameId(gameId);
        log.setCreatedAt(timestamp);
        log.setEntry(timestamp + " | player=" + playerId + " | action=" + action + " | result=" + result);
        logs.save(log);
    }

    @Override
    public List<String> actionLog(UUID gameId) {
        return logs.findByGameIdOrderByCreatedAtAsc(gameId).stream().map(GameActionLogEntity::getEntry).toList();
    }

    @Override
    public Optional<GameAggregate> latestSnapshot(UUID gameId) {
        return snapshots.findTopByGameIdOrderByVersionDesc(gameId).map(e -> deserialize(e.getPayload()));
    }

    private String serialize(GameAggregate game) {
        try {
            return objectMapper.writeValueAsString(game);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize game aggregate", e);
        }
    }

    private GameAggregate deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, GameAggregate.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize game aggregate", e);
        }
    }
}
