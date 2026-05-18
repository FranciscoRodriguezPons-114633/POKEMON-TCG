package com.utn.pokemontcg.realtime.presentation;

import com.utn.pokemontcg.realtime.application.EventBroadcastService;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class RealtimeController {

    private final EventBroadcastService broadcastService;

    public RealtimeController(EventBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @PostMapping("/internal/events")
    public ResponseEntity<Map<String, String>> publishFromGameService(@Valid @RequestBody GameEventMessage event) {
        broadcastService.broadcast(event);
        return ResponseEntity.ok(Map.of("status", "PUBLISHED"));
    }

    @GetMapping("/internal/events/{gameId}")
    public ResponseEntity<Map<String, List<GameEventMessage>>> pendingEvents(
        @PathVariable("gameId") UUID gameId,
        @RequestParam(name = "since", required = false) Instant since
    ) {
        return ResponseEntity.ok(Map.of("events", broadcastService.eventsSince(gameId, since)));
    }
}
