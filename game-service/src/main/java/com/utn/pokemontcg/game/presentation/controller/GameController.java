package com.utn.pokemontcg.game.presentation.controller;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.PlayerSetupState;
import com.utn.pokemontcg.game.presentation.dto.CreateGameRequest;
import com.utn.pokemontcg.game.presentation.dto.GameActionRequest;
import com.utn.pokemontcg.game.presentation.dto.GameSyncResponse;
import com.utn.pokemontcg.game.presentation.dto.GameStateResponse;
import com.utn.pokemontcg.game.presentation.dto.JoinGameRequest;
import com.utn.pokemontcg.game.presentation.dto.SetupGameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
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
        return ResponseEntity.ok(toResponse(gameService.create(request.playerId(), request.deckId())));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateResponse> join(@PathVariable UUID gameId, @Valid @RequestBody JoinGameRequest request) {
        return ResponseEntity.ok(toResponse(gameService.join(gameId, request.playerId(), request.deckId())));
    }

    @PostMapping("/{gameId}/setup")
    public ResponseEntity<GameStateResponse> setup(@PathVariable UUID gameId, @Valid @RequestBody SetupGameRequest request) {
        GameAggregate game = gameService.runInitialSetup(
            gameId,
            request.playerOneDeckSize(), request.playerOneBasicCount(),
            request.playerTwoDeckSize(), request.playerTwoBasicCount(),
            request.playerOneDeckId(), request.playerTwoDeckId()
        );
        return ResponseEntity.ok(toResponse(game));
    }

    @PostMapping("/{gameId}/actions")
    public ResponseEntity<GameStateResponse> action(@PathVariable UUID gameId,
                                                     @Valid @RequestBody GameActionRequest request) {
        return ResponseEntity.ok(toResponse(gameService.executeAction(gameId, request.actionType(), request.condition())));
    }

    @GetMapping("/{gameId}/actions")
    public ResponseEntity<Map<String, List<String>>> actions(@PathVariable UUID gameId) {
        return ResponseEntity.ok(Map.of("entries", gameService.actionLog(gameId)));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> state(@PathVariable UUID gameId) {
        return ResponseEntity.ok(toResponse(gameService.get(gameId)));
    }

    @GetMapping("/{gameId}/logs")
    public ResponseEntity<Map<String, List<String>>> actionLog(@PathVariable UUID gameId) {
        return ResponseEntity.ok(Map.of("entries", gameService.actionLog(gameId)));
    }

    @GetMapping("/{gameId}/sync")
    public ResponseEntity<GameSyncResponse> sync(@PathVariable UUID gameId) {
        GameAggregate game = gameService.get(gameId);
        return ResponseEntity.ok(new GameSyncResponse(
            toResponse(game),
            gameService.actionLog(gameId),
            Instant.now()
        ));
    }

    private GameStateResponse toResponse(GameAggregate game) {
        return new GameStateResponse(
            game.id(),
            game.gameState().name(),
            game.turnPhase().name(),
            game.currentTurnPlayer(),
            game.firstPlayer(),
            game.winner(),
            setupSummary(game),
            game.activePokemon(),
            game.bench(),
            handSizes(game),
            game.deckCardsRemaining(),
            game.prizeCardsRemaining(),
            game.discardPile(),
            game.activeDamageCounters(),
            game.activeAttachedEnergy(),
            game.statusByPlayer()
        );
    }

    private Map<UUID, PlayerSetupState> setupSummary(GameAggregate game) {
        Map<UUID, PlayerSetupState> setup = new HashMap<>();
        game.setupByPlayer().forEach((player, original) -> {
            PlayerSetupState current = new PlayerSetupState();
            current.setMulligans(original.mulligans());
            current.setHandSize(game.hand().getOrDefault(player, List.of()).size());
            current.setBenchCount(game.bench().getOrDefault(player, List.of()).size());
            current.setPrizeCount(game.prizeCards().getOrDefault(player, List.of()).size());
            current.setHasActive(game.activePokemon().get(player) != null);
            setup.put(player, current);
        });
        return setup;
    }

    private Map<UUID, Integer> handSizes(GameAggregate game) {
        Map<UUID, Integer> sizes = new HashMap<>();
        game.hand().forEach((player, hand) -> sizes.put(player, hand.size()));
        return sizes;
    }
}
