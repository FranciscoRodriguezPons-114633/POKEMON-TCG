package com.utn.pokemontcg.game.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "game_snapshots")
public class GameSnapshotEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID gameId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant updatedAt;

    public UUID getGameId() { return gameId; }
    public void setGameId(UUID gameId) { this.gameId = gameId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
