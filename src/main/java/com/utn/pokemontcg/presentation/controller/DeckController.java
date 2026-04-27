package com.utn.pokemontcg.presentation.controller;

import com.utn.pokemontcg.application.service.DeckService;
import com.utn.pokemontcg.presentation.dto.request.DeckValidationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(@Valid @RequestBody DeckValidationRequest request) {
        deckService.validateDeck(request.cardsByApiId(), request.hasAceSpec(), request.hasBasicPokemon());
        return ResponseEntity.ok(Map.of("status", "VALID"));
    }
}
