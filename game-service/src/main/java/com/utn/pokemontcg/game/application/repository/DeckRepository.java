package com.utn.pokemontcg.game.application.repository;

import java.util.Map;
import java.util.UUID;

public interface DeckRepository {
    void save(UUID playerId, Map<String, Integer> cards);
}
