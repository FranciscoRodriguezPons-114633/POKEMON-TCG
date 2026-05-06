package com.utn.pokemontcg.game.domain.model;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameAggregate {
    private final UUID id;
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
    private final Map<UUID, Integer> deckCardsRemaining = new HashMap<>();
    private final Map<UUID, EnumSet<StatusCondition>> statusByPlayer = new HashMap<>();
    private UUID winner;

    public GameAggregate(UUID playerOne) {
        this.id = UUID.randomUUID();
        this.playerOne = playerOne;
        this.gameState = GameState.WAITING;
        this.turnPhase = TurnPhase.DRAW;
        this.updatedAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID playerOne() { return playerOne; }
    public UUID playerTwo() { return playerTwo; }
    public void setPlayerTwo(UUID playerTwo) { this.playerTwo = playerTwo; touch(); }
    public UUID currentTurnPlayer() { return currentTurnPlayer; }
    public void setCurrentTurnPlayer(UUID currentTurnPlayer) { this.currentTurnPlayer = currentTurnPlayer; touch(); }
    public GameState gameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; touch(); }
    public TurnPhase turnPhase() { return turnPhase; }
    public void setTurnPhase(TurnPhase turnPhase) { this.turnPhase = turnPhase; touch(); }
    public Instant updatedAt() { return updatedAt; }
    public Map<UUID, PlayerSetupState> setupByPlayer() { return setupByPlayer; }
    public UUID firstPlayer() { return firstPlayer; }
    public void setFirstPlayer(UUID firstPlayer) { this.firstPlayer = firstPlayer; touch(); }
    public boolean firstTurn() { return firstTurn; }
    public void setFirstTurn(boolean firstTurn) { this.firstTurn = firstTurn; touch(); }
    public TurnFlags turnFlags() { return turnFlags; }
    public Map<UUID, Integer> prizeCardsRemaining() { return prizeCardsRemaining; }
    public Map<UUID, Integer> activeHp() { return activeHp; }
    public Map<UUID, Integer> deckCardsRemaining() { return deckCardsRemaining; }
    public Map<UUID, EnumSet<StatusCondition>> statusByPlayer() { return statusByPlayer; }
    public UUID winner() { return winner; }
    public void setWinner(UUID winner) { this.winner = winner; touch(); }

    private void touch() { this.updatedAt = Instant.now(); }
}
