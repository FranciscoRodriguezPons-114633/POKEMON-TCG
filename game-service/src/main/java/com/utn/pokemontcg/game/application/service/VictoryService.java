package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VictoryService {

    public void applyKnockoutAndPrizes(GameAggregate game, UUID attacker, UUID defender) {
        if (attacker == null || defender == null) return;

        if (game.activeHp().getOrDefault(defender, 1) > 0) return;
        if (game.activePokemon().get(defender) == null) return;

        int prizeLoss = game.activePokemonEx().getOrDefault(defender, false) ? 2 : 1;

        discardKnockedOutActive(game, defender);
        takePrizeCards(game, attacker, prizeLoss);

        promoteFromBenchIfPossible(game, defender);
    }

    public UUID checkWinner(GameAggregate game) {
        UUID p1 = game.playerOne();
        UUID p2 = game.playerTwo();

        if (p1 == null || p2 == null) return null;

        boolean p1PrizeWin = game.prizeCardsRemaining().getOrDefault(p1, 6) <= 0;
        boolean p2PrizeWin = game.prizeCardsRemaining().getOrDefault(p2, 6) <= 0;
        boolean p1NoPokemon = !game.hasPokemonInPlay(p1);
        boolean p2NoPokemon = !game.hasPokemonInPlay(p2);

        // Sudden death if both players meet win conditions simultaneously
        if ((p1PrizeWin && p2PrizeWin) || (p1NoPokemon && p2NoPokemon)) {
            startSuddenDeath(game);
            return null;
        }

        if (p1PrizeWin || p2NoPokemon) return p1;
        if (p2PrizeWin || p1NoPokemon) return p2;

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

    private void discardKnockedOutActive(GameAggregate game, UUID player) {
        String active = game.activePokemon().remove(player);
        if (active != null) {
            game.discardPile().computeIfAbsent(player, ignored -> new java.util.ArrayList<>()).add(active);
        }
        game.activeAttachedEnergy().put(player, 0);
        game.activeDamageCounters().put(player, 0);
        game.activeHp().put(player, 0);
        game.activePokemonEx().put(player, false);
        game.statusByPlayer().remove(player);
    }

    private void takePrizeCards(GameAggregate game, UUID player, int amount) {
        List<String> prizes = game.prizeCards().getOrDefault(player, List.of());
        List<String> hand = game.hand().computeIfAbsent(player, ignored -> new java.util.ArrayList<>());
        int taken = 0;
        while (taken < amount && !prizes.isEmpty()) {
            hand.add(prizes.remove(0));
            taken++;
        }
        game.prizeCardsRemaining().put(player, prizes.size());
    }

    private void promoteFromBenchIfPossible(GameAggregate game, UUID player) {
        List<String> bench = game.bench().getOrDefault(player, List.of());
        if (bench.isEmpty()) {
            return;
        }
        String promoted = bench.remove(0);
        game.activePokemon().put(player, promoted);
        game.activeHp().put(player, game.activeMaxHp(player));
        game.activeDamageCounters().put(player, 0);
        game.activeAttachedEnergy().put(player, 0);
        var card = game.cardCatalog().get(promoted);
        game.activePokemonEx().put(player, card != null && card.isPokemonEx());
    }
}
