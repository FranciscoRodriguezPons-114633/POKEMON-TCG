package com.utn.pokemontcg.domain;

import com.utn.pokemontcg.domain.engine.GameEngineFacade;
import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.domain.model.GameState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineFacadeTest {

    private final GameEngineFacade facade = new GameEngineFacade();

    @Test
    void shouldStartGameWhenTwoPlayersJoin() {
        Game game = new Game(UUID.randomUUID(), UUID.randomUUID());
        game.join(UUID.randomUUID(), UUID.randomUUID());

        facade.startGame(game);

        assertEquals(GameState.ACTIVE, game.getState());
    }
}
