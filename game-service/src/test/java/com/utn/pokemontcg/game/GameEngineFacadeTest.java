package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineFacadeTest {

    @Test
    void shouldMoveToBetweenTurnsAfterAttackResolution() {
        GameEngineFacade facade = new GameEngineFacade(new GameEventPublisher());
        GameAggregate game = new GameAggregate(UUID.randomUUID());
        game.setTurnPhase(TurnPhase.ATTACK);

        facade.resolveAttack(game);

        assertEquals(TurnPhase.BETWEEN_TURNS, game.turnPhase());
    }
}
