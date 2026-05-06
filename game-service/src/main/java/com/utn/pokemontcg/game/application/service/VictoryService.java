package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VictoryService {

    public UUID checkWinner(GameAggregate game) {
        UUID p1 = game.playerOne();
        UUID p2 = game.playerTwo();
        if (p1 == null || p2 == null) return null;

        if (game.prizeCardsRemaining().getOrDefault(p1, 6) <= 0) return p1;
        if (game.prizeCardsRemaining().getOrDefault(p2, 6) <= 0) return p2;

        if (game.activeHp().getOrDefault(p1, 1) <= 0) return p2;
        if (game.activeHp().getOrDefault(p2, 1) <= 0) return p1;

        if (game.deckCardsRemaining().getOrDefault(p1, 1) <= 0) return p2;
        if (game.deckCardsRemaining().getOrDefault(p2, 1) <= 0) return p1;
        return null;
    }

    public void closeGameIfNeeded(GameAggregate game) {
        UUID winner = checkWinner(game);
        if (winner != null) {
            game.setWinner(winner);
            game.setGameState(GameState.FINISHED);
        }
    }
}
