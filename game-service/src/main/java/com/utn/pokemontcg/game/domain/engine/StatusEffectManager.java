package com.utn.pokemontcg.game.domain.engine;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.UUID;

@Component
public class StatusEffectManager {

    private final SecureRandom random = new SecureRandom();

    public void applyCondition(GameAggregate game, UUID target, StatusCondition condition) {
        EnumSet<StatusCondition> statuses = game.statusByPlayer().computeIfAbsent(target, ignored -> EnumSet.noneOf(StatusCondition.class));

        if (condition == StatusCondition.ASLEEP || condition == StatusCondition.CONFUSED || condition == StatusCondition.PARALYZED) {
            statuses.remove(StatusCondition.ASLEEP);
            statuses.remove(StatusCondition.CONFUSED);
            statuses.remove(StatusCondition.PARALYZED);
        }
        statuses.add(condition);
    }

    public void resolveBetweenTurns(GameAggregate game, UUID target) {
        resolveBetweenTurns(game, target, random.nextBoolean(), random.nextBoolean());
    }

    public void resolveBetweenTurns(GameAggregate game, UUID target, boolean burnedTails, boolean asleepHeads) {
        EnumSet<StatusCondition> statuses = game.statusByPlayer().getOrDefault(target, EnumSet.noneOf(StatusCondition.class));

        if (statuses.contains(StatusCondition.POISONED)) {
            addDamageCounters(game, target, 1);
        }
        if (statuses.contains(StatusCondition.BURNED) && burnedTails) {
            addDamageCounters(game, target, 2);
        }
        if (statuses.contains(StatusCondition.ASLEEP) && asleepHeads) {
            statuses.remove(StatusCondition.ASLEEP);
        }
        if (statuses.contains(StatusCondition.PARALYZED)) {
            statuses.remove(StatusCondition.PARALYZED);
        }
        game.statusByPlayer().put(target, statuses);
    }

    public void clearSpecialConditions(GameAggregate game, UUID target) {
        game.statusByPlayer().remove(target);
    }

    private void addDamageCounters(GameAggregate game, UUID target, int counters) {
        int newCounters = game.activeDamageCounters().getOrDefault(target, 0) + counters;
        game.activeDamageCounters().put(target, newCounters);
        game.activeHp().put(target, Math.max(0, game.activeMaxHp(target) - (newCounters * 10)));
    }
}
