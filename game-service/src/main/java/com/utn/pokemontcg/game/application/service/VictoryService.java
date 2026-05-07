package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class VictoryService {

    public void applyKnockoutAndPrizes(GameAggregate game, UUID attacker, UUID defender, int defenderPrizeLoss) {
        if (attacker == null || defender == null) return;
        if (game.activeHp().getOrDefault(defender, 1) > 0) return;

        int currentPrizes = game.prizeCardsRemaining().getOrDefault(attacker, 6);
        game.prizeCardsRemaining().put(attacker, Math.max(0, currentPrizes - defenderPrizeLoss));
        game.activeHp().put(defender, 120);
        game.statusByPlayer().remove(defender);
    }

    public UUID checkWinner(GameAggregate game) {
        UUID p1 = game.playerOne();
        UUID p2 = game.playerTwo();
        if (p1 == null || p2 == null) return null;

        boolean p1PrizeWin = game.prizeCardsRemaining().getOrDefault(p1, 6) <= 0;
        boolean p2PrizeWin = game.prizeCardsRemaining().getOrDefault(p2, 6) <= 0;

        boolean p1DeckOut = game.deckCardsRemaining().getOrDefault(p1, 1) <= 0;
        boolean p2DeckOut = game.deckCardsRemaining().getOrDefault(p2, 1) <= 0;

        if ((p1PrizeWin && p2PrizeWin) || (p1DeckOut && p2DeckOut)) {
            startSuddenDeath(game);
            return null;
        }

        if (p1PrizeWin || p2DeckOut) return p1;
        if (p2PrizeWin || p1DeckOut) return p2;

        return null;
    }

    public void closeGameIfNeeded(GameAggregate game) {
        UUID winner = checkWinner(game);
        if (winner != null) {
            game.setWinner(winner);
            game.setGameState(GameState.FINISHED);
        }
    }

    private void startSuddenDeath(GameAggregate game) {
        game.setGameState(GameState.SETUP);
        game.prizeCardsRemaining().put(game.playerOne(), 1);
        game.prizeCardsRemaining().put(game.playerTwo(), 1);
        game.setWinner(null);
    }
}
