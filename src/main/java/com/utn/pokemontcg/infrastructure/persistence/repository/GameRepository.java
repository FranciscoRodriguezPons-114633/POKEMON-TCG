package com.utn.pokemontcg.infrastructure.persistence.repository;

import com.utn.pokemontcg.infrastructure.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
}
