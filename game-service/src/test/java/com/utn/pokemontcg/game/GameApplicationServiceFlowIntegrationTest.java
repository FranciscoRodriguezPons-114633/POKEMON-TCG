package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GameApplicationServiceFlowIntegrationTest {

    @Autowired
    private GameApplicationService gameApplicationService;

    @Test
    void createJoinSetupTurnAttack_flow_is_consistent() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        var game = gameApplicationService.create(p1);
        gameApplicationService.join(game.id(), p2);
        var afterSetup = gameApplicationService.runInitialSetup(game.id(), 60, 12, 60, 12);

        assertEquals(TurnPhase.MAIN, afterSetup.turnPhase());

        gameApplicationService.executeAction(game.id(), GameActionType.ATTACH_ENERGY);
        gameApplicationService.executeAction(game.id(), GameActionType.PLAY_SUPPORTER);
        gameApplicationService.executeAction(game.id(), GameActionType.RETREAT);

        var beforeAttackPhase = gameApplicationService.get(game.id());
        beforeAttackPhase.setTurnPhase(TurnPhase.ATTACK);
        beforeAttackPhase.setFirstTurn(false);

        var afterAttack = gameApplicationService.executeAction(game.id(), GameActionType.ATTACK);
        assertEquals(TurnPhase.BETWEEN_TURNS, afterAttack.turnPhase());
    }
}
