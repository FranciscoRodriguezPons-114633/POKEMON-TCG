package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventAccessRegistryTest {

    @Test
    void shouldAllowOnlyRegisteredGamePlayers() {
        EventAccessRegistry registry = new EventAccessRegistry();
        UUID gameId = UUID.randomUUID();
        UUID playerOne = UUID.randomUUID();

        registry.register(new GameEventEnvelope(
            1L,
            1,
            gameId,
            GameEventType.GAME_CREATED,
            Map.of("playerOne", playerOne.toString()),
            Instant.parse("2026-05-06T18:00:00Z"),
            Instant.parse("2026-05-06T18:00:01Z")
        ));

        assertTrue(registry.canSubscribe(gameId, playerOne));
        assertFalse(registry.canSubscribe(gameId, UUID.randomUUID()));
    }
}

