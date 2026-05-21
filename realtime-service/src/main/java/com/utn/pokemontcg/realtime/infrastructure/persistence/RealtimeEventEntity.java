package com.utn.pokemontcg.realtime.infrastructure.persistence;

import com.utn.pokemontcg.realtime.dto.GameEventType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "realtime_events")
public class RealtimeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private GameEventType type;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected RealtimeEventEntity() {
    }

    public RealtimeEventEntity(
        int schemaVersion,
        UUID gameId,
        GameEventType type,
        String payloadJson,
        Instant occurredAt,
        Instant receivedAt
    ) {
        this.schemaVersion = schemaVersion;
        this.gameId = gameId;
        this.type = type;
        this.payloadJson = payloadJson;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
    }

    public Long sequence() {
        return sequence;
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public UUID gameId() {
        return gameId;
    }

    public GameEventType type() {
        return type;
    }

    public String payloadJson() {
        return payloadJson;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public Instant receivedAt() {
        return receivedAt;
    }

    public void assignSequenceForTest(long sequence) {
        this.sequence = sequence;
    }
}

