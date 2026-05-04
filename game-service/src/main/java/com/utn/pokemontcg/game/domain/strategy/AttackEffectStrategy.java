package com.utn.pokemontcg.game.domain.strategy;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public interface AttackEffectStrategy {
    String attackId();
    int computeBaseDamage(GameAggregate game);
}
