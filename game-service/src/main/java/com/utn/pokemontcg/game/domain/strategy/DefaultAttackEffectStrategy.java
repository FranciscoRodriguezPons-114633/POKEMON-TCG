package com.utn.pokemontcg.game.domain.strategy;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public class DefaultAttackEffectStrategy implements AttackEffectStrategy {
    @Override
    public String attackId() {
        return "DEFAULT";
    }

    @Override
    public int computeBaseDamage(GameAggregate game) {
        return 10;
    }
}
