package com.utn.pokemontcg.presentation.controller;

import com.utn.pokemontcg.application.realtime.GameRealtimeService;
import com.utn.pokemontcg.application.service.GameService;
import com.utn.pokemontcg.presentation.dto.request.CreateGameRequest;
import com.utn.pokemontcg.presentation.dto.request.JoinGameRequest;
import com.utn.pokemontcg.presentation.dto.response.GameStateResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameRealtimeService gameRealtimeService;

    public GameController(GameService gameService, GameRealtimeService gameRealtimeService) {
        this.gameService = gameService;
        this.gameRealtimeService = gameRealtimeService;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> create(@Valid @RequestBody CreateGameRequest request) {
        GameStateResponse response = GameStateResponse.from(gameService.createGame(request.playerId(), request.deckId()));
        gameRealtimeService.publishGameState(response.gameId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateResponse> join(@PathVariable UUID gameId, @Valid @RequestBody JoinGameRequest request) {
        GameStateResponse response = GameStateResponse.from(gameService.joinGame(gameId, request.playerId(), request.deckId()));
        gameRealtimeService.publishGameState(gameId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> state(@PathVariable UUID gameId) {
        return ResponseEntity.ok(GameStateResponse.from(gameService.getById(gameId)));
    }

    @PostMapping("/{gameId}/attack")
    public ResponseEntity<GameStateResponse> attack(@PathVariable UUID gameId) {
        GameStateResponse response = GameStateResponse.from(gameService.resolveAttack(gameId));
        gameRealtimeService.publishGameState(gameId);
        return ResponseEntity.ok(response);
    }
}
