package com.utn.pokemontcg.presentation.controller;

import com.utn.pokemontcg.application.service.cards.CardApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardApiService cardApiService;

    public CardController(CardApiService cardApiService) {
        this.cardApiService = cardApiService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam(defaultValue = "set.id:xy1") String query,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(cardApiService.search(query, pageSize));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String cardId) {
        return ResponseEntity.ok(cardApiService.getById(cardId));
    }
}
