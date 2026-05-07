package com.utn.pokemontcg.game.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameAggregate {
    private UUID id;
    private UUID playerOne;
    private UUID playerTwo;
    private UUID currentTurnPlayer;
    private GameState gameState;
    private TurnPhase turnPhase;
    private Instant updatedAt;
    private Map<UUID, PlayerSetupState> setupByPlayer = new HashMap<>();
    private UUID firstPlayer;
    private boolean firstTurn = true;
    private TurnFlags turnFlags = new TurnFlags();
    private Map<UUID, Integer> prizeCardsRemaining = new HashMap<>();
    private Map<UUID, Integer> activeHp = new HashMap<>();
    private Map<UUID, Integer> deckCardsRemaining = new HashMap<>();
    private Map<UUID, EnumSet<StatusCondition>> statusByPlayer = new HashMap<>();
    private Map<UUID, Boolean> activePokemonEx = new HashMap<>();
    private UUID winner;

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

    public UUID id() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID playerOne() { return playerOne; }
    public void setPlayerOne(UUID playerOne) { this.playerOne = playerOne; touch(); }
    public UUID playerTwo() { return playerTwo; }
    public void setPlayerTwo(UUID playerTwo) { this.playerTwo = playerTwo; touch(); }
    public UUID currentTurnPlayer() { return currentTurnPlayer; }
    public void setCurrentTurnPlayer(UUID currentTurnPlayer) { this.currentTurnPlayer = currentTurnPlayer; touch(); }
    public GameState gameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; touch(); }
    public TurnPhase turnPhase() { return turnPhase; }
    public void setTurnPhase(TurnPhase turnPhase) { this.turnPhase = turnPhase; touch(); }
    public Instant updatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Map<UUID, PlayerSetupState> setupByPlayer() { return setupByPlayer; }
    public void setSetupByPlayer(Map<UUID, PlayerSetupState> setupByPlayer) { this.setupByPlayer = setupByPlayer; }
    public UUID firstPlayer() { return firstPlayer; }
    public void setFirstPlayer(UUID firstPlayer) { this.firstPlayer = firstPlayer; touch(); }
    public boolean firstTurn() { return firstTurn; }
    public void setFirstTurn(boolean firstTurn) { this.firstTurn = firstTurn; touch(); }
    public TurnFlags turnFlags() { return turnFlags; }
    public void setTurnFlags(TurnFlags turnFlags) { this.turnFlags = turnFlags; }
    public Map<UUID, Integer> prizeCardsRemaining() { return prizeCardsRemaining; }
    public void setPrizeCardsRemaining(Map<UUID, Integer> prizeCardsRemaining) { this.prizeCardsRemaining = prizeCardsRemaining; }
    public Map<UUID, Integer> activeHp() { return activeHp; }
    public void setActiveHp(Map<UUID, Integer> activeHp) { this.activeHp = activeHp; }
    public Map<UUID, Integer> deckCardsRemaining() { return deckCardsRemaining; }
    public void setDeckCardsRemaining(Map<UUID, Integer> deckCardsRemaining) { this.deckCardsRemaining = deckCardsRemaining; }
    public Map<UUID, EnumSet<StatusCondition>> statusByPlayer() { return statusByPlayer; }
    public void setStatusByPlayer(Map<UUID, EnumSet<StatusCondition>> statusByPlayer) { this.statusByPlayer = statusByPlayer; }
    public Map<UUID, Boolean> activePokemonEx() { return activePokemonEx; }
    public void setActivePokemonEx(Map<UUID, Boolean> activePokemonEx) { this.activePokemonEx = activePokemonEx; }
    public UUID winner() { return winner; }
    public void setWinner(UUID winner) { this.winner = winner; touch(); }

    @JsonIgnore
    private void touch() { this.updatedAt = Instant.now(); }
}
