package com.utn.pokemontcg.game.domain.strategy;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public class SupporterEffectStrategy implements TrainerEffectStrategy {
    @Override
    public String type() {
        return "SUPPORTER";
    }

    @Override
    public void apply(GameAggregate game) {
        // Placeholder for supporter-specific rules
    }
}
