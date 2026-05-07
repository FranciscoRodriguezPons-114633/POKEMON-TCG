package com.utn.pokemontcg.game.infrastructure.persistence.deck;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeckJpaRepository extends JpaRepository<DeckEntity, UUID> {
    List<DeckEntity> findByPlayerId(UUID playerId);
}
