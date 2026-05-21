package com.utn.pokemontcg.realtime.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface RealtimeEventJpaRepository extends JpaRepository<RealtimeEventEntity, Long> {

    List<RealtimeEventEntity> findAllByOrderBySequenceAsc();

    List<RealtimeEventEntity> findByGameIdOrderBySequenceAsc(UUID gameId);

    List<RealtimeEventEntity> findByGameIdAndSequenceGreaterThanOrderBySequenceAsc(UUID gameId, long sequence);

    Optional<RealtimeEventEntity> findTopByGameIdOrderBySequenceDesc(UUID gameId);
}

