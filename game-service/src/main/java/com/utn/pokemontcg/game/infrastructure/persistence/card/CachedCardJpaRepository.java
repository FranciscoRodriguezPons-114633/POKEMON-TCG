package com.utn.pokemontcg.game.infrastructure.persistence.card;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CachedCardJpaRepository extends JpaRepository<CachedCardEntity, String> {
}
