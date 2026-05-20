package com.utn.pokemontcg.realtime.presentation;

import com.utn.pokemontcg.realtime.application.EventBroadcastService;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.PendingEventsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
public class RealtimeController {

    private final EventBroadcastService broadcastService;

    public RealtimeController(EventBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @PostMapping("/internal/events")
    public ResponseEntity<GameEventEnvelope> publishFromGameService(@Valid @RequestBody GameEventMessage event) {
        return ResponseEntity.ok(broadcastService.broadcast(event));
    }

    @GetMapping("/internal/events/{gameId}")
    public ResponseEntity<PendingEventsResponse> pendingEvents(
        @PathVariable("gameId") UUID gameId,
        @RequestParam(name = "sinceSequence", required = false) Long sinceSequence,
        @RequestParam(name = "since", required = false) Instant since
    ) {
        return ResponseEntity.ok(broadcastService.pendingEvents(gameId, sinceSequence, since));
    }
}
