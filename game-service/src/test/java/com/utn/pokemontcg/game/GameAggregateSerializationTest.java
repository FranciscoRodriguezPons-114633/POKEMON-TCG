package com.utn.pokemontcg.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameAggregateSerializationTest {

    @Test
    void serializesAndRestoresAggregateSnapshots() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        UUID playerOne = UUID.randomUUID();
        UUID gameId;

        GameAggregate game = new GameAggregate(playerOne);
        gameId = game.id();
        game.initializeZonesFor(playerOne, List.of("xy1-1", "xy1-2"));
        game.hand().get(playerOne).add("xy1-3");

        String json = objectMapper.writeValueAsString(game);
        GameAggregate restored = objectMapper.readValue(json, GameAggregate.class);

        assertEquals(gameId, restored.id());
        assertEquals(playerOne, restored.playerOne());
        assertEquals(List.of("xy1-1", "xy1-2"), restored.deck().get(playerOne));
        assertEquals(List.of("xy1-3"), restored.hand().get(playerOne));
    }
}
