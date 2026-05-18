package com.utn.pokemontcg.game.domain.facade;

import com.utn.pokemontcg.game.domain.chain.*;
import com.utn.pokemontcg.game.domain.event.GameEvent;
import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.event.GameEventType;
import com.utn.pokemontcg.game.domain.model.GameCard;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.state.*;

import java.util.List;
import java.util.Map;

public class GameEngineFacade {

    private final GameEventPublisher eventPublisher;

    public GameEngineFacade(GameEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void startSetup(GameAggregate game) {
        new SetupState().enter(game);
        eventPublisher.publish(GameEvent.of(game.id(), GameEventType.GAME_SETUP, Map.of("state", game.gameState().name())));
    }

    public void startActive(GameAggregate game) {
        new ActiveState().enter(game);
        new DrawPhaseState().advance(game);
        eventPublisher.publish(GameEvent.of(game.id(), GameEventType.TURN_STARTED, Map.of(
            "phase", game.turnPhase().name(),
            "currentTurnPlayer", game.currentTurnPlayer()
        )));
    }

    public void advanceTurnPhase(GameAggregate game) {
        TurnPhaseState state = switch (game.turnPhase()) {
            case DRAW -> new DrawPhaseState();
            case MAIN -> new MainPhaseState();
            case ATTACK -> new AttackPhaseState();
            case BETWEEN_TURNS -> new BetweenTurnsPhaseState();
        };
        state.advance(game);
        eventPublisher.publish(GameEvent.of(game.id(), GameEventType.TURN_PHASE_CHANGED, Map.of("phase", game.turnPhase().name())));
    }

    public AttackContext resolveAttack(GameAggregate game) {
        AttackResolutionPipeline pipeline = new AttackResolutionPipeline(List.of(
            new EnergyValidationStep(),
            new ConfusionCheckStep(),
            new SelectionStep(),
            new PreAttackStep(),
            new ModifierStep(),
            new DamageCalculationStep(),
            new PostDamageEffectsStep()
        ));
        AttackContext context = buildDefaultAttackContext(game);
        pipeline.resolve(context);
        eventPublisher.publish(GameEvent.of(game.id(), GameEventType.ATTACK_RESOLVED, Map.of(
            "damage", context.damage(),
            "auditTrail", context.auditTrail()
        )));
        game.setTurnPhase(com.utn.pokemontcg.game.domain.model.TurnPhase.BETWEEN_TURNS);
        return context;
    }

    public void publish(GameAggregate game, GameEventType type, Map<String, Object> payload) {
        eventPublisher.publish(GameEvent.of(game.id(), type, payload));
    }

    private AttackContext buildDefaultAttackContext(GameAggregate game) {
        AttackContext context = new AttackContext(game);
        var attacker = game.currentTurnPlayer();
        GameCard activeCard = game.cardCatalog().get(game.activePokemon().get(attacker));
        int requiredEnergy = activeCard != null ? activeCard.attackRequiredEnergy() : game.activeAttackRequiredEnergy();
        int baseDamage = activeCard != null ? activeCard.attackDamage() : game.activeAttackBaseDamage();
        if (game.activeAttackRequiredEnergy() != 1) {
            requiredEnergy = game.activeAttackRequiredEnergy();
        }
        if (game.activeAttackBaseDamage() != 30) {
            baseDamage = game.activeAttackBaseDamage();
        }
        context.setRequiredEnergy(requiredEnergy);
        context.setAttachedEnergy(game.activeAttachedEnergy().getOrDefault(attacker, 0));
        context.setConfused(game.statusByPlayer()
            .getOrDefault(attacker, java.util.EnumSet.noneOf(com.utn.pokemontcg.game.domain.model.StatusCondition.class))
            .contains(com.utn.pokemontcg.game.domain.model.StatusCondition.CONFUSED));
        context.setBaseDamage(baseDamage);
        context.setWeaknessMultiplier(game.defendingPokemonWeakToAttack() ? 2 : 1);
        context.setResistanceReduction(game.defendingPokemonResistsAttack() ? 20 : 0);
        return context;
    }
}
