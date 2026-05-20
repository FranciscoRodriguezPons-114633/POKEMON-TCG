package com.utn.pokemontcg.card.presentation;

import com.utn.pokemontcg.card.application.CardApiService;
import com.utn.pokemontcg.card.application.dto.CardSearchResponse;
import com.utn.pokemontcg.card.application.dto.CardSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardApiService cardApiService;

    public CardController(CardApiService cardApiService) {
        this.cardApiService = cardApiService;
    }

    @GetMapping
    public ResponseEntity<CardSearchResponse> search(@RequestParam(defaultValue = "set.id:xy1") String q,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(cardApiService.search(q, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardSummaryResponse> byId(@PathVariable String id) {
        return ResponseEntity.ok(cardApiService.byId(id));
    }
}
