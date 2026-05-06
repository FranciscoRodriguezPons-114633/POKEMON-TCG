package com.utn.pokemontcg.game.application.repository;

import java.util.Map;
import java.util.Optional;

public interface CardRepository {
    Optional<Map<String, Object>> findById(String cardId);
}
