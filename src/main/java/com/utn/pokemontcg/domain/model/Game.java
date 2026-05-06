package com.utn.pokemontcg.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private final UUID id;
    private final List<PlayerSlot> players = new ArrayList<>(2);
    private GameState state;
    private TurnPhase phase;
    private UUID currentTurnPlayer;
    private UUID winnerId;
    private final Instant createdAt;

    public Game(UUID creatorId, UUID creatorDeckId) {
        this.id = UUID.randomUUID();
        this.players.add(new PlayerSlot(creatorId, creatorDeckId));
        this.state = GameState.WAITING;
        this.phase = TurnPhase.DRAW;
        this.createdAt = Instant.now();
    }

    public void join(UUID playerId, UUID deckId) {
        if (players.size() >= 2) throw new IllegalStateException("La partida ya tiene 2 jugadores");
        players.add(new PlayerSlot(playerId, deckId));
        this.state = GameState.SETUP;
    }

    public void activate(UUID firstPlayerId) {
        this.state = GameState.ACTIVE;
        this.phase = TurnPhase.DRAW;
        this.currentTurnPlayer = firstPlayerId;
    }

    public void nextPhase() {
        this.phase = switch (phase) {
            case DRAW -> TurnPhase.MAIN;
            case MAIN -> TurnPhase.ATTACK;
            case ATTACK -> TurnPhase.BETWEEN_TURNS;
            case BETWEEN_TURNS -> TurnPhase.DRAW;
        };
    }

    public void finish(UUID winnerId) {
        this.state = GameState.FINISHED;
        this.winnerId = winnerId;
    }

    public UUID getId() { return id; }
    public List<PlayerSlot> getPlayers() { return List.copyOf(players); }
    public GameState getState() { return state; }
    public TurnPhase getPhase() { return phase; }
    public UUID getCurrentTurnPlayer() { return currentTurnPlayer; }
    public UUID getWinnerId() { return winnerId; }
    public Instant getCreatedAt() { return createdAt; }
}
