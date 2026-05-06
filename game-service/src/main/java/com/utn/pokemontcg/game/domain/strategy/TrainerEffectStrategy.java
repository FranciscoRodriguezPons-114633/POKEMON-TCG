package com.utn.pokemontcg.game.domain.strategy;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public interface TrainerEffectStrategy {
    String type();
    void apply(GameAggregate game);
}
