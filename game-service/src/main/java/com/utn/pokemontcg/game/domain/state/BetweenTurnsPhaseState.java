package com.utn.pokemontcg.game.domain.state;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.TurnPhase;

public class BetweenTurnsPhaseState implements TurnPhaseState {
    @Override
    public void advance(GameAggregate game) {
        game.setTurnPhase(TurnPhase.DRAW);
    }
}
