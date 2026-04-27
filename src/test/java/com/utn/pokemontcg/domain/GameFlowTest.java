package com.utn.pokemontcg.domain;

import com.utn.pokemontcg.application.service.GameService;
import com.utn.pokemontcg.domain.engine.GameEngineFacade;
import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.domain.model.TurnPhase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameFlowTest {

    @Test
    void shouldAdvancePhasesViaRealtimeActionsContract() {
        GameService gameService = new GameService(new GameEngineFacade());

        Game created = gameService.createGame(UUID.randomUUID(), UUID.randomUUID());
        Game active = gameService.joinGame(created.getId(), UUID.randomUUID(), UUID.randomUUID());

        assertEquals(TurnPhase.MAIN, active.getPhase());

        Game attackPhase = gameService.endMainPhase(created.getId());
        assertEquals(TurnPhase.ATTACK, attackPhase.getPhase());

        Game betweenTurns = gameService.resolveAttack(created.getId());
        assertEquals(TurnPhase.BETWEEN_TURNS, betweenTurns.getPhase());

        Game nextTurn = gameService.nextTurn(created.getId());
        assertEquals(TurnPhase.MAIN, nextTurn.getPhase());
    }
}
