package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.*;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.infrastructure.repository.InMemoryGameStateRepository;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameApplicationServiceFlowIntegrationTest {

    @Test
    void createJoinSetupTurnAttack_flow_is_consistent() {

        GameApplicationService gameApplicationService =
                new GameApplicationService(
                        new InMemoryGameStateRepository(),
                        new GameEngineFacade(new GameEventPublisher()),
                        new SetupEngineService(),
                        new TurnActionValidator(),
                        new VictoryService(),
                        new RuleValidator(),
                        new DamageCalculator(),
                        new StatusEffectManager()
                );

        UUID p1 = UUID.randomUUID();

        UUID p2 = UUID.randomUUID();

        var game = gameApplicationService.create(p1);

        gameApplicationService.join(game.id(), p2);

        var afterSetup =
                gameApplicationService.runInitialSetup(
                        game.id(),
                        60,
                        12,
                        60,
                        12
                );

        assertEquals(TurnPhase.MAIN, afterSetup.turnPhase());

        gameApplicationService.executeAction(
                game.id(),
                GameActionType.ATTACH_ENERGY
        );

        gameApplicationService.executeAction(
                game.id(),
                GameActionType.PLAY_SUPPORTER
        );

        gameApplicationService.executeAction(
                game.id(),
                GameActionType.RETREAT
        );

        var beforeAttackPhase =
                gameApplicationService.get(game.id());

        beforeAttackPhase.setTurnPhase(TurnPhase.ATTACK);

        beforeAttackPhase.setFirstTurn(false);

        var afterAttack =
                gameApplicationService.executeAction(
                        game.id(),
                        GameActionType.ATTACK
                );

        assertEquals(
                TurnPhase.BETWEEN_TURNS,
                afterAttack.turnPhase()
        );
    }
}