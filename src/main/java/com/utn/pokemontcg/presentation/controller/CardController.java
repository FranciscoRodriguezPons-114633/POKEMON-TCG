package com.utn.pokemontcg.presentation.controller;

import com.utn.pokemontcg.infrastructure.external.pokemontcg.PokemonTcgClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final PokemonTcgClient pokemonTcgClient;

    public CardController(PokemonTcgClient pokemonTcgClient) {
        this.pokemonTcgClient = pokemonTcgClient;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam(defaultValue = "set.id:xy1") String query,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(pokemonTcgClient.searchCards(query, pageSize));
    }
}
