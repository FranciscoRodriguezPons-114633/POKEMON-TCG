package com.utn.pokemontcg.game.domain.state;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public interface GameLifecycleState {
    void enter(GameAggregate game);
}
