package com.utn.pokemontcg.game.presentation.controller;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.presentation.dto.CreateGameRequest;
import com.utn.pokemontcg.game.presentation.dto.GameActionRequest;
import com.utn.pokemontcg.game.presentation.dto.JoinGameRequest;
import com.utn.pokemontcg.game.presentation.dto.SetupGameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
        return ResponseEntity.ok(toResponse(gameService.create(request.playerId())));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<Map<String, Object>> join(@PathVariable UUID gameId, @Valid @RequestBody JoinGameRequest request) {
        return ResponseEntity.ok(toResponse(gameService.join(gameId, request.playerId())));
    }

    @PostMapping("/{gameId}/setup")
    public ResponseEntity<Map<String, Object>> setup(@PathVariable UUID gameId, @Valid @RequestBody SetupGameRequest request) {
        GameAggregate game = gameService.runInitialSetup(
            gameId,
            request.playerOneDeckSize(), request.playerOneBasicCount(),
            request.playerTwoDeckSize(), request.playerTwoBasicCount()
        );
        return ResponseEntity.ok(toResponse(game));
    }

    @PostMapping("/{gameId}/actions")
    public ResponseEntity<Map<String, Object>> action(@PathVariable UUID gameId,
                                                       @Valid @RequestBody GameActionRequest request) {
        return ResponseEntity.ok(toResponse(gameService.executeAction(gameId, request.actionType())));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Map<String, Object>> state(@PathVariable UUID gameId) {
        return ResponseEntity.ok(toResponse(gameService.get(gameId)));
    }

    private Map<String, Object> toResponse(GameAggregate game) {
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.id());
        response.put("state", game.gameState().name());
        response.put("phase", game.turnPhase().name());
        response.put("currentTurnPlayer", game.currentTurnPlayer());
        response.put("firstPlayer", game.firstPlayer());
        response.put("setupByPlayer", game.setupByPlayer());
        return response;
    }
}
