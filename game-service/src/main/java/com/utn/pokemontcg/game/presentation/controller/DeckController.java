package com.utn.pokemontcg.game.presentation.controller;

import com.utn.pokemontcg.game.application.service.deck.DeckService;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckResponse;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping
    public ResponseEntity<DeckResponse> create(@Valid @RequestBody DeckUpsertRequest request) {
        return ResponseEntity.ok(deckService.create(request));
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> update(@PathVariable("deckId") UUID deckId, @Valid @RequestBody DeckUpsertRequest request) {
        return ResponseEntity.ok(deckService.update(deckId, request));
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<DeckResponse> get(@PathVariable("deckId") UUID deckId) {
        return ResponseEntity.ok(deckService.get(deckId));
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> delete(@PathVariable("deckId") UUID deckId) {
        deckService.delete(deckId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> listByPlayer(@RequestParam("playerId") UUID playerId) {
        return ResponseEntity.ok(deckService.listByPlayer(playerId));
    }
}
