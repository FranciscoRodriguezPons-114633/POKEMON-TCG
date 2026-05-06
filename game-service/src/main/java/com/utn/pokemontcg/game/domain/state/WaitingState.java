package com.utn.pokemontcg.game.domain.state;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;

public class WaitingState implements GameLifecycleState {
    @Override
    public void enter(GameAggregate game) {
        game.setGameState(GameState.WAITING);
    }
}
