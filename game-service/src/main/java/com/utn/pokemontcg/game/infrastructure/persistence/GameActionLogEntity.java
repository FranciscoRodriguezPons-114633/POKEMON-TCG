package com.utn.pokemontcg.game.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "game_action_logs")
public class GameActionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID gameId;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String entry;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public UUID getGameId() { return gameId; }
    public void setGameId(UUID gameId) { this.gameId = gameId; }
    public String getEntry() { return entry; }
    public void setEntry(String entry) { this.entry = entry; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
