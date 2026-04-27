package com.utn.pokemontcg.game.presentation.controller;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.presentation.dto.CreateGameRequest;
import com.utn.pokemontcg.game.presentation.dto.GameActionRequest;
import com.utn.pokemontcg.game.presentation.dto.JoinGameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameApplicationService gameService;

    public GameController(GameApplicationService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateGameRequest request) {
        GameAggregate game = gameService.create(request.playerId());
        return ResponseEntity.ok(Map.of("gameId", game.id(), "state", game.gameState().name(), "phase", game.turnPhase().name()));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<Map<String, Object>> join(@PathVariable UUID gameId, @Valid @RequestBody JoinGameRequest request) {
        GameAggregate game = gameService.join(gameId, request.playerId());
        return ResponseEntity.ok(Map.of("gameId", game.id(), "state", game.gameState().name(), "phase", game.turnPhase().name()));
    }

    @PostMapping("/{gameId}/actions")
    public ResponseEntity<Map<String, Object>> action(@PathVariable UUID gameId,
                                                       @Valid @RequestBody GameActionRequest request) {
        GameAggregate game = gameService.executeAction(gameId, request.actionType());
        return ResponseEntity.ok(Map.of("gameId", game.id(), "state", game.gameState().name(), "phase", game.turnPhase().name()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Map<String, Object>> state(@PathVariable UUID gameId) {
        GameAggregate game = gameService.get(gameId);
        return ResponseEntity.ok(Map.of("gameId", game.id(), "state", game.gameState().name(), "phase", game.turnPhase().name()));
    }
}
