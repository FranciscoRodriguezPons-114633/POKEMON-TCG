package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventAccessRegistry {

    private final Map<UUID, Set<UUID>> playersByGame = new ConcurrentHashMap<>();

    public void register(GameEventEnvelope event) {
        if (event.type() == GameEventType.GAME_CREATED) {
            addPlayer(event.gameId(), event.payload().get("playerOne"));
        }
        if (event.type() == GameEventType.PLAYER_JOINED) {
            addPlayer(event.gameId(), event.payload().get("playerTwo"));
        }
    }

    public boolean canSubscribe(UUID gameId, UUID playerId) {
        return playersByGame.getOrDefault(gameId, Set.of()).contains(playerId);
    }

    private void addPlayer(UUID gameId, Object rawPlayerId) {
        UUID playerId = parseUuid(rawPlayerId);
        if (playerId == null) {
            return;
        }
        playersByGame
            .computeIfAbsent(gameId, ignored -> ConcurrentHashMap.newKeySet())
            .add(playerId);
    }

    private UUID parseUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String text && !text.isBlank()) {
            return UUID.fromString(text);
        }
        return null;
    }
}

