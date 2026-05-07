package com.utn.pokemontcg.game.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameSnapshotJpaRepository extends JpaRepository<GameSnapshotEntity, UUID> {
}
