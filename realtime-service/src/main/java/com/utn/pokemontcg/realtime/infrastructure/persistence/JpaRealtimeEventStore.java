package com.utn.pokemontcg.realtime.infrastructure.persistence;

import com.utn.pokemontcg.realtime.application.RealtimeEventStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRealtimeEventStore implements RealtimeEventStore {

    private final RealtimeEventJpaRepository repository;

    public JpaRealtimeEventStore(RealtimeEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RealtimeEventEntity save(RealtimeEventEntity event) {
        return repository.save(event);
    }

    @Override
    public List<RealtimeEventEntity> findAllOrdered() {
        return repository.findAllByOrderBySequenceAsc();
    }

    @Override
    public List<RealtimeEventEntity> findByGameId(UUID gameId) {
        return repository.findByGameIdOrderBySequenceAsc(gameId);
    }

    @Override
    public List<RealtimeEventEntity> findByGameIdAfterSequence(UUID gameId, long sequence) {
        return repository.findByGameIdAndSequenceGreaterThanOrderBySequenceAsc(gameId, sequence);
    }

    @Override
    public Optional<RealtimeEventEntity> findLatestByGameId(UUID gameId) {
        return repository.findTopByGameIdOrderBySequenceDesc(gameId);
    }
}

