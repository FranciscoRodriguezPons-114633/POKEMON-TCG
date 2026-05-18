package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.application.service.SetupEngineService;
import com.utn.pokemontcg.game.application.service.TurnActionValidator;
import com.utn.pokemontcg.game.application.service.VictoryService;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.infrastructure.repository.InMemoryGameStateRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRulesProgressTest {

    @Test
    void setupCreatesRealGameZones() {
        GameApplicationService service = service();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        var game = service.create(p1);
        service.join(game.id(), p2);
        var setup = service.runInitialSetup(game.id(), 60, 14, 60, 14);

        assertNotNull(setup.activePokemon().get(p1));
        assertNotNull(setup.activePokemon().get(p2));
        assertEquals(6, setup.prizeCards().get(p1).size());
        assertEquals(6, setup.prizeCards().get(p2).size());
        assertFalse(setup.hand().get(p1).isEmpty());
        assertTrue(setup.deckCardsRemaining().get(p1) < 60);
    }

    @Test
    void knockoutWithoutBenchFinishesByTotalKnockout() {
        GameApplicationService service = service();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        var game = service.create(p1);
        service.join(game.id(), p2);
        var setup = service.runInitialSetup(game.id(), 60, 14, 60, 14);

        setup.setCurrentTurnPlayer(p1);
        setup.setFirstTurn(false);
        setup.setTurnPhase(TurnPhase.ATTACK);
        setup.setActiveAttackBaseDamage(120);
        setup.activeAttachedEnergy().put(p1, 1);
        setup.bench().get(p2).clear();

        var afterAttack = service.executeAction(game.id(), GameActionType.ATTACK);

        assertEquals(GameState.FINISHED, afterAttack.gameState());
        assertEquals(p1, afterAttack.winner());
    }

    @Test
    void specialConditionsRespectReplacementAndBetweenTurnsOrder() {
        StatusEffectManager manager = new StatusEffectManager();
        UUID player = UUID.randomUUID();
        var game = new com.utn.pokemontcg.game.domain.model.GameAggregate(player);
        game.activeHp().put(player, 120);
        game.activeDamageCounters().put(player, 0);

        manager.applyCondition(game, player, StatusCondition.ASLEEP);
        manager.applyCondition(game, player, StatusCondition.CONFUSED);
        manager.applyCondition(game, player, StatusCondition.POISONED);
        manager.applyCondition(game, player, StatusCondition.BURNED);
        manager.resolveBetweenTurns(game, player, true, false);

        assertFalse(game.statusByPlayer().get(player).contains(StatusCondition.ASLEEP));
        assertTrue(game.statusByPlayer().get(player).contains(StatusCondition.CONFUSED));
        assertTrue(game.statusByPlayer().get(player).contains(StatusCondition.POISONED));
        assertTrue(game.statusByPlayer().get(player).contains(StatusCondition.BURNED));
        assertEquals(3, game.activeDamageCounters().get(player));
        assertEquals(90, game.activeHp().get(player));
    }

    private GameApplicationService service() {
        return new GameApplicationService(
            new InMemoryGameStateRepository(),
            new GameEngineFacade(new GameEventPublisher()),
            new SetupEngineService(),
            new TurnActionValidator(),
            new VictoryService(),
            new RuleValidator(),
            new DamageCalculator(),
            new StatusEffectManager()
        );
    }
}
