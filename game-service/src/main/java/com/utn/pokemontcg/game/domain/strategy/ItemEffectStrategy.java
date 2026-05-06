package com.utn.pokemontcg.game.domain.strategy;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public class ItemEffectStrategy implements TrainerEffectStrategy {
    @Override
    public String type() {
        return "ITEM";
    }

    @Override
    public void apply(GameAggregate game) {
        // Placeholder for item-specific rules
    }
}
