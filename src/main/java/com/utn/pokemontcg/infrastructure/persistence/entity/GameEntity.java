package com.utn.pokemontcg.infrastructure.persistence.entity;

import com.utn.pokemontcg.domain.model.GameState;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameState state;

    @Column(name = "current_turn_player_id")
    private UUID currentTurnPlayerId;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public UUID getCurrentTurnPlayerId() { return currentTurnPlayerId; }
    public void setCurrentTurnPlayerId(UUID currentTurnPlayerId) { this.currentTurnPlayerId = currentTurnPlayerId; }
    public UUID getWinnerId() { return winnerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
