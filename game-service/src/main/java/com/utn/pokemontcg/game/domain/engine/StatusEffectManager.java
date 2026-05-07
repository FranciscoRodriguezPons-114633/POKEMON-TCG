package com.utn.pokemontcg.game.domain.engine;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Component
public class StatusEffectManager {

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
        EnumSet<StatusCondition> statuses = game.statusByPlayer().getOrDefault(target, EnumSet.noneOf(StatusCondition.class));

        if (statuses.contains(StatusCondition.POISONED)) {
            game.activeHp().put(target, game.activeHp().getOrDefault(target, 120) - 10);
        }
        if (statuses.contains(StatusCondition.BURNED)) {
            game.activeHp().put(target, game.activeHp().getOrDefault(target, 120) - 20);
        }
        if (statuses.contains(StatusCondition.PARALYZED)) {
            statuses.remove(StatusCondition.PARALYZED);
        }
        game.statusByPlayer().put(target, statuses);
    }
}
