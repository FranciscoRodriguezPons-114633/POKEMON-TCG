package com.utn.pokemontcg.game.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class GameAggregate {

    private UUID id;

    private UUID playerOne;
    private UUID playerTwo;
    private UUID currentTurnPlayer;

    private GameState gameState;
    private TurnPhase turnPhase;

    private Instant updatedAt;

    private final Map<UUID, PlayerSetupState> setupByPlayer = new HashMap<>();

    private UUID firstPlayer;
    private boolean firstTurn = true;

    private final TurnFlags turnFlags = new TurnFlags();

    private final Map<UUID, Integer> prizeCardsRemaining = new HashMap<>();
    private final Map<UUID, Integer> activeHp = new HashMap<>();
    private final Map<UUID, Integer> activeDamageCounters = new HashMap<>();
    private final Map<UUID, Integer> activeAttachedEnergy = new HashMap<>();
    private final Map<UUID, Integer> deckCardsRemaining = new HashMap<>();
    private final Map<UUID, EnumSet<StatusCondition>> statusByPlayer = new HashMap<>();

    private final Map<UUID, Boolean> activePokemonEx = new HashMap<>();
    private final Map<UUID, String> activePokemon = new HashMap<>();
    private final Map<UUID, List<String>> bench = new HashMap<>();
    private final Map<UUID, List<String>> hand = new HashMap<>();
    private final Map<UUID, List<String>> deck = new HashMap<>();
    private final Map<UUID, List<String>> prizeCards = new HashMap<>();
    private final Map<UUID, List<String>> discardPile = new HashMap<>();
    private final Map<String, GameCard> cardCatalog = new HashMap<>();

    private int activeAttackBaseDamage = 30;
    private int activeAttackRequiredEnergy = 1;
    private boolean defendingPokemonWeakToAttack;
    private boolean defendingPokemonResistsAttack;

    private UUID winner;

    // ========================
    // Constructors
    // ========================

    public GameAggregate() {
        this.id = UUID.randomUUID();
        this.gameState = GameState.WAITING;
        this.turnPhase = TurnPhase.DRAW;
        this.updatedAt = Instant.now();
    }

    public GameAggregate(UUID playerOne) {
        this();
        this.playerOne = playerOne;
    }

    // ========================
    // Getters
    // ========================

    public UUID id() { return id; }

    public UUID playerOne() { return playerOne; }

    public UUID playerTwo() { return playerTwo; }

    public UUID currentTurnPlayer() { return currentTurnPlayer; }

    public GameState gameState() { return gameState; }

    public TurnPhase turnPhase() { return turnPhase; }

    public Instant updatedAt() { return updatedAt; }

    public Map<UUID, PlayerSetupState> setupByPlayer() { return setupByPlayer; }

    public UUID firstPlayer() { return firstPlayer; }

    public boolean firstTurn() { return firstTurn; }

    public TurnFlags turnFlags() { return turnFlags; }

    public Map<UUID, Integer> prizeCardsRemaining() { return prizeCardsRemaining; }

    public Map<UUID, Integer> activeHp() { return activeHp; }

    public Map<UUID, Integer> activeDamageCounters() { return activeDamageCounters; }

    public Map<UUID, Integer> activeAttachedEnergy() { return activeAttachedEnergy; }

    public Map<UUID, Integer> deckCardsRemaining() { return deckCardsRemaining; }

    public Map<UUID, EnumSet<StatusCondition>> statusByPlayer() { return statusByPlayer; }

    public Map<UUID, Boolean> activePokemonEx() { return activePokemonEx; }

    public Map<UUID, String> activePokemon() { return activePokemon; }

    public Map<UUID, List<String>> bench() { return bench; }

    public Map<UUID, List<String>> hand() { return hand; }

    public Map<UUID, List<String>> deck() { return deck; }

    public Map<UUID, List<String>> prizeCards() { return prizeCards; }

    public Map<UUID, List<String>> discardPile() { return discardPile; }

    public Map<String, GameCard> cardCatalog() { return cardCatalog; }

    public int activeAttackBaseDamage() { return activeAttackBaseDamage; }

    public int activeAttackRequiredEnergy() { return activeAttackRequiredEnergy; }

    public boolean defendingPokemonWeakToAttack() { return defendingPokemonWeakToAttack; }

    public boolean defendingPokemonResistsAttack() { return defendingPokemonResistsAttack; }

    public UUID winner() { return winner; }

    // ========================
    // Setters
    // ========================

    public void setPlayerOne(UUID playerOne) {
        this.playerOne = playerOne;
        touch();
    }

    public void setPlayerTwo(UUID playerTwo) {
        this.playerTwo = playerTwo;
        touch();
    }

    public void setCurrentTurnPlayer(UUID currentTurnPlayer) {
        this.currentTurnPlayer = currentTurnPlayer;
        touch();
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        touch();
    }

    public void setTurnPhase(TurnPhase turnPhase) {
        this.turnPhase = turnPhase;
        touch();
    }

    public void setFirstPlayer(UUID firstPlayer) {
        this.firstPlayer = firstPlayer;
        touch();
    }

    public void setFirstTurn(boolean firstTurn) {
        this.firstTurn = firstTurn;
        touch();
    }

    public void setWinner(UUID winner) {
        this.winner = winner;
        touch();
    }

    public void setActiveAttackBaseDamage(int activeAttackBaseDamage) {
        this.activeAttackBaseDamage = activeAttackBaseDamage;
        touch();
    }

    public void setActiveAttackRequiredEnergy(int activeAttackRequiredEnergy) {
        this.activeAttackRequiredEnergy = activeAttackRequiredEnergy;
        touch();
    }

    public void setDefendingPokemonWeakToAttack(boolean defendingPokemonWeakToAttack) {
        this.defendingPokemonWeakToAttack = defendingPokemonWeakToAttack;
        touch();
    }

    public void setDefendingPokemonResistsAttack(boolean defendingPokemonResistsAttack) {
        this.defendingPokemonResistsAttack = defendingPokemonResistsAttack;
        touch();
    }

    public void initializeZonesFor(UUID player, List<String> orderedDeck) {
        deck.put(player, new ArrayList<>(orderedDeck));
        hand.put(player, new ArrayList<>());
        prizeCards.put(player, new ArrayList<>());
        discardPile.put(player, new ArrayList<>());
        bench.put(player, new ArrayList<>());
        activePokemon.remove(player);
        activeDamageCounters.put(player, 0);
        activeAttachedEnergy.put(player, 0);
        deckCardsRemaining.put(player, orderedDeck.size());
        touch();
    }

    public void registerCard(GameCard card) {
        cardCatalog.put(card.id(), card);
        touch();
    }

    public int activeMaxHp(UUID player) {
        GameCard card = cardCatalog.get(activePokemon.get(player));
        return card != null && card.hp() > 0 ? card.hp() : 120;
    }

    public boolean hasPokemonInPlay(UUID player) {
        return activePokemon.get(player) != null
            || !bench.getOrDefault(player, List.of()).isEmpty();
    }

    // ========================
    // Internal
    // ========================

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
