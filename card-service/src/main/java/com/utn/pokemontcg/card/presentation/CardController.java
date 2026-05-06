package com.utn.pokemontcg.card.presentation;

import com.utn.pokemontcg.card.application.CardApiService;
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
    public ResponseEntity<Map<String, Object>> search(@RequestParam(defaultValue = "set.id:xy1") String q,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(cardApiService.search(q, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> byId(@PathVariable String id) {
        return ResponseEntity.ok(cardApiService.byId(id));
    }
}
