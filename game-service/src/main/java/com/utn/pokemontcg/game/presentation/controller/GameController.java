package com.utn.pokemontcg.game.presentation.controller;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.presentation.dto.CreateGameRequest;
import com.utn.pokemontcg.game.presentation.dto.GameActionRequest;
import com.utn.pokemontcg.game.presentation.dto.GameStateResponse;
import com.utn.pokemontcg.game.presentation.dto.JoinGameRequest;
import com.utn.pokemontcg.game.presentation.dto.SetupGameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<GameStateResponse> create(@Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.ok(toResponse(gameService.create(request.playerId())));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateResponse> join(@PathVariable UUID gameId, @Valid @RequestBody JoinGameRequest request) {
        return ResponseEntity.ok(toResponse(gameService.join(gameId, request.playerId())));
    }

    @PostMapping("/{gameId}/setup")
    public ResponseEntity<GameStateResponse> setup(@PathVariable UUID gameId, @Valid @RequestBody SetupGameRequest request) {
        GameAggregate game = gameService.runInitialSetup(
            gameId,
            request.playerOneDeckSize(), request.playerOneBasicCount(),
            request.playerTwoDeckSize(), request.playerTwoBasicCount()
        );
        return ResponseEntity.ok(toResponse(game));
    }

    @PostMapping("/{gameId}/actions")
    public ResponseEntity<GameStateResponse> action(@PathVariable UUID gameId,
                                                     @Valid @RequestBody GameActionRequest request) {
        return ResponseEntity.ok(toResponse(gameService.executeAction(gameId, request.actionType())));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> state(@PathVariable UUID gameId) {
        return ResponseEntity.ok(toResponse(gameService.get(gameId)));
    }

    @GetMapping("/{gameId}/logs")
    public ResponseEntity<Map<String, List<String>>> actionLog(@PathVariable UUID gameId) {
        return ResponseEntity.ok(Map.of("entries", gameService.actionLog(gameId)));
    }

    private GameStateResponse toResponse(GameAggregate game) {
        return new GameStateResponse(
            game.id(),
            game.gameState().name(),
            game.turnPhase().name(),
            game.currentTurnPlayer(),
            game.firstPlayer(),
            game.setupByPlayer()
        );
    }
}
