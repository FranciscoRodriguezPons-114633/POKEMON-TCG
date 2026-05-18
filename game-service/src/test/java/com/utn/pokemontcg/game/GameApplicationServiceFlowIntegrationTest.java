package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.*;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.infrastructure.repository.InMemoryGameStateRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameApplicationServiceFlowIntegrationTest {

    @Test
    void createJoinSetupTurnAttack_flow_is_consistent() {
        // Instanciación manual: Rápida, limpia y desacoplada del framework
        GameApplicationService gameApplicationService = new GameApplicationService(
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

        // 1. Creación y Join
        var game = gameApplicationService.create(p1);
        gameApplicationService.join(game.id(), p2);

        // 2. Setup Inicial
        var afterSetup = gameApplicationService.runInitialSetup(
                game.id(), 60, 12, 60, 12
        );

        assertEquals(TurnPhase.MAIN, afterSetup.turnPhase());

        // 3. Acciones de Turno
        gameApplicationService.executeAction(game.id(), GameActionType.ATTACH_ENERGY);
        gameApplicationService.executeAction(game.id(), GameActionType.PLAY_SUPPORTER);
        gameApplicationService.executeAction(game.id(), GameActionType.RETREAT);
        var afterMain = gameApplicationService.executeAction(game.id(), GameActionType.END_TURN);

        assertEquals(TurnPhase.ATTACK, afterMain.turnPhase());

        // 4. Simulación de Fase de Ataque
        var beforeAttackPhase = gameApplicationService.get(game.id());
        beforeAttackPhase.setTurnPhase(TurnPhase.ATTACK);
        beforeAttackPhase.setFirstTurn(false);

        var afterAttack = gameApplicationService.executeAction(game.id(), GameActionType.ATTACK);

        // 5. Verificación de cambio de fase
        assertEquals(TurnPhase.BETWEEN_TURNS, afterAttack.turnPhase());

        var nextTurn = gameApplicationService.executeAction(game.id(), GameActionType.END_TURN);
        UUID nextPlayer = nextTurn.currentTurnPlayer();
        int deckBeforeDraw = nextTurn.deckCardsRemaining().get(nextPlayer);

        var afterDraw = gameApplicationService.executeAction(game.id(), GameActionType.DRAW);

        assertEquals(TurnPhase.MAIN, afterDraw.turnPhase());
        assertEquals(deckBeforeDraw - 1, afterDraw.deckCardsRemaining().get(nextPlayer));
        assertEquals(afterDraw.deck().get(nextPlayer).size(), afterDraw.deckCardsRemaining().get(nextPlayer));
    }

    @Test
    void specialConditionCanBeSelectedAndBetweenTurnsResolutionStartsNextTurn() {
        GameApplicationService gameApplicationService = new GameApplicationService(
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
        var setup = gameApplicationService.runInitialSetup(game.id(), 60, 12, 60, 12);

        UUID activePlayer = setup.currentTurnPlayer();
        UUID defender = activePlayer.equals(p1) ? p2 : p1;
        setup.setTurnPhase(TurnPhase.BETWEEN_TURNS);

        var conditioned = gameApplicationService.executeAction(
            game.id(),
            GameActionType.APPLY_SPECIAL_CONDITION,
            StatusCondition.BURNED
        );

        assertEquals(StatusCondition.BURNED, conditioned.statusByPlayer().get(defender).iterator().next());

        var nextTurn = gameApplicationService.executeAction(game.id(), GameActionType.RESOLVE_BETWEEN_TURNS);

        assertEquals(TurnPhase.DRAW, nextTurn.turnPhase());
        assertEquals(defender, nextTurn.currentTurnPlayer());
    }
}
