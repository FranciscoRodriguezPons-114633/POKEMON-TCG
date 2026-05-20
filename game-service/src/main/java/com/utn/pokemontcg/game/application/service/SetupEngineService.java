package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameCard;
import com.utn.pokemontcg.game.domain.model.PlayerSetupState;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SetupEngineService {

    private final SecureRandom random = new SecureRandom();

    public PlayerSetupState preparePlayer(int deckSize, int basicCount, int opponentMulligans) {
        if (deckSize < 7) {
            throw new IllegalArgumentException("El mazo debe tener al menos 7 cartas para iniciar setup");
        }
        if (basicCount <= 0) {
            throw new IllegalArgumentException("El mazo debe tener al menos 1 Pokemon Basico");
        }

        PlayerSetupState state = new PlayerSetupState();

        int mulligans = 0;
        while (!drawContainsBasic(deckSize, basicCount, 7)) {
            mulligans++;
        }

        state.setMulligans(mulligans);
        state.setHasActive(true);
        state.setBenchCount(random.nextInt(6));
        state.setPrizeCount(6);
        state.setHandSize(7 + opponentMulligans);
        return state;
    }

    public PlayerSetupState preparePlayerBoard(UUID player, int deckSize, int basicCount, int opponentMulligans,
                                               GameAggregate game) {
        PlayerSetupState state = preparePlayer(deckSize, basicCount, opponentMulligans);
        int mulligans = 0;
        List<String> deck;
        List<String> hand;

        do {
            deck = buildDeck(player, deckSize, basicCount, game);
            Collections.shuffle(deck, random);
            hand = draw(deck, 7);
            if (containsBasic(hand)) {
                break;
            }
            mulligans++;
        } while (true);

        game.initializeZonesFor(player, deck);
        game.hand().put(player, hand);

        String active = removeFirstBasic(hand);
        game.activePokemon().put(player, active);
        game.activeHp().put(player, game.activeMaxHp(player));
        game.activeDamageCounters().put(player, 0);
        game.activeAttachedEnergy().put(player, 0);
        game.activePokemonEx().put(player, game.cardCatalog().get(active).isPokemonEx());

        List<String> bench = game.bench().get(player);
        while (bench.size() < 5) {
            String basic = removeFirstBasic(hand);
            if (basic == null) {
                break;
            }
            bench.add(basic);
        }

        game.prizeCards().put(player, draw(deck, 6));
        game.deck().put(player, new ArrayList<>(deck));
        game.prizeCardsRemaining().put(player, game.prizeCards().get(player).size());
        game.deckCardsRemaining().put(player, deck.size());

        state.setMulligans(mulligans);
        state.setHasActive(true);
        state.setBenchCount(bench.size());
        state.setPrizeCount(game.prizeCards().get(player).size());
        state.setHandSize(hand.size() + opponentMulligans);
        return state;
    }

    public PlayerSetupState preparePlayerBoard(UUID player, List<GameCard> deckCards, int opponentMulligans,
                                               GameAggregate game) {
        if (deckCards.size() < 7) {
            throw new IllegalArgumentException("El mazo debe tener al menos 7 cartas para iniciar setup");
        }
        if (deckCards.stream().noneMatch(GameCard::isBasicPokemon)) {
            throw new IllegalArgumentException("El mazo debe tener al menos 1 Pokemon Basico");
        }

        PlayerSetupState state = new PlayerSetupState();
        int mulligans = 0;
        List<String> deck;
        List<String> hand;

        do {
            deck = buildDeck(deckCards, game);
            Collections.shuffle(deck, random);
            hand = draw(deck, 7);
            if (containsBasic(hand, game)) {
                break;
            }
            mulligans++;
        } while (true);

        setupBoardFromOpeningHand(player, opponentMulligans, game, state, mulligans, deck, hand);
        return state;
    }

    private boolean drawContainsBasic(int deckSize, int basicCount, int drawCount) {
        int successes = 0;
        for (int i = 0; i < drawCount; i++) {
            int remainingDeck = deckSize - i;
            int remainingBasics = basicCount - successes;
            if (remainingBasics <= 0) {
                continue;
            }
            double chance = (double) remainingBasics / remainingDeck;
            if (random.nextDouble() < chance) {
                successes++;
            }
        }
        return successes > 0;
    }

    private List<String> buildDeck(UUID player, int deckSize, int basicCount, GameAggregate game) {
        List<String> cards = new ArrayList<>(deckSize);
        for (int i = 0; i < basicCount; i++) {
            String id = "xy1-basic-" + player + "-" + i;
            cards.add(id);
            game.registerCard(new GameCard(
                id,
                "Starter Basic " + i,
                "Pokemon",
                Set.of("Basic"),
                120,
                30,
                1
            ));
        }
        for (int i = basicCount; i < deckSize; i++) {
            String id = "xy1-energy-" + player + "-" + i;
            cards.add(id);
            game.registerCard(new GameCard(
                id,
                "Basic Energy " + i,
                "Energy",
                Set.of("Basic"),
                0,
                0,
                0
            ));
        }
        return cards;
    }

    private List<String> buildDeck(List<GameCard> deckCards, GameAggregate game) {
        List<String> cards = new ArrayList<>(deckCards.size());
        for (GameCard card : deckCards) {
            cards.add(card.id());
            game.registerCard(card);
        }
        return cards;
    }

    private List<String> draw(List<String> deck, int count) {
        List<String> drawn = new ArrayList<>();
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            drawn.add(deck.remove(0));
        }
        return drawn;
    }

    private boolean containsBasic(List<String> cards) {
        return cards.stream().anyMatch(card -> card.startsWith("xy1-basic-"));
    }

    private boolean containsBasic(List<String> cards, GameAggregate game) {
        return cards.stream().anyMatch(card -> game.cardCatalog().get(card).isBasicPokemon());
    }

    private String removeFirstBasic(List<String> cards) {
        for (int i = 0; i < cards.size(); i++) {
            String card = cards.get(i);
            if (card.startsWith("xy1-basic-")) {
                cards.remove(i);
                return card;
            }
        }
        return null;
    }

    private String removeFirstBasic(List<String> cards, GameAggregate game) {
        for (int i = 0; i < cards.size(); i++) {
            String card = cards.get(i);
            if (game.cardCatalog().get(card).isBasicPokemon()) {
                cards.remove(i);
                return card;
            }
        }
        return null;
    }

    private void setupBoardFromOpeningHand(UUID player, int opponentMulligans, GameAggregate game,
                                           PlayerSetupState state, int mulligans, List<String> deck,
                                           List<String> hand) {
        game.initializeZonesFor(player, deck);
        game.hand().put(player, hand);

        String active = removeFirstBasic(hand, game);
        game.activePokemon().put(player, active);
        game.activeHp().put(player, game.activeMaxHp(player));
        game.activeDamageCounters().put(player, 0);
        game.activeAttachedEnergy().put(player, 0);
        game.activePokemonEx().put(player, game.cardCatalog().get(active).isPokemonEx());

        List<String> bench = game.bench().get(player);
        while (bench.size() < 5) {
            String basic = removeFirstBasic(hand, game);
            if (basic == null) {
                break;
            }
            bench.add(basic);
        }

        game.prizeCards().put(player, draw(deck, 6));
        game.deck().put(player, new ArrayList<>(deck));
        game.prizeCardsRemaining().put(player, game.prizeCards().get(player).size());
        game.deckCardsRemaining().put(player, deck.size());

        state.setMulligans(mulligans);
        state.setHasActive(true);
        state.setBenchCount(bench.size());
        state.setPrizeCount(game.prizeCards().get(player).size());
        state.setHandSize(hand.size() + opponentMulligans);
    }
}
