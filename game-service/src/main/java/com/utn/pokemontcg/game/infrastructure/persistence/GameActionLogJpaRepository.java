package com.utn.pokemontcg.game.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameActionLogJpaRepository extends JpaRepository<GameActionLogEntity, Long> {
    List<GameActionLogEntity> findByGameIdOrderByCreatedAtAsc(java.util.UUID gameId);
}
