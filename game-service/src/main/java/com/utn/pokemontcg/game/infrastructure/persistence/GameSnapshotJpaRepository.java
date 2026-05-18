package com.utn.pokemontcg.game.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameSnapshotJpaRepository extends JpaRepository<GameSnapshotEntity, Long> {
    Optional<GameSnapshotEntity> findTopByGameIdOrderByVersionDesc(UUID gameId);
    long countByGameId(UUID gameId);
}
