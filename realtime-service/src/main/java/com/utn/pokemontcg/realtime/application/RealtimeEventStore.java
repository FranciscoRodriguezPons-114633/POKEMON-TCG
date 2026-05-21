package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.infrastructure.persistence.RealtimeEventEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RealtimeEventStore {

    RealtimeEventEntity save(RealtimeEventEntity event);

    List<RealtimeEventEntity> findAllOrdered();

    List<RealtimeEventEntity> findByGameId(UUID gameId);

    List<RealtimeEventEntity> findByGameIdAfterSequence(UUID gameId, long sequence);

    Optional<RealtimeEventEntity> findLatestByGameId(UUID gameId);
}

