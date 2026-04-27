package com.utn.pokemontcg.application.service.cards;

import com.utn.pokemontcg.infrastructure.external.pokemontcg.PokemonTcgClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CardApiService {

    private final PokemonTcgClient pokemonTcgClient;
    private final CardCacheService cardCacheService;

    public CardApiService(PokemonTcgClient pokemonTcgClient, CardCacheService cardCacheService) {
        this.pokemonTcgClient = pokemonTcgClient;
        this.cardCacheService = cardCacheService;
    }

    public Map<String, Object> search(String query, int pageSize) {
        String key = "cards:search:" + query + ":" + pageSize;
        return cardCacheService.get(key)
            .orElseGet(() -> {
                Map<String, Object> response = pokemonTcgClient.searchCards(query, pageSize);
                cardCacheService.put(key, response);
                return response;
            });
    }

    public Map<String, Object> getById(String cardId) {
        String key = "cards:id:" + cardId;
        return cardCacheService.get(key)
            .orElseGet(() -> {
                Map<String, Object> response = pokemonTcgClient.getCardById(cardId);
                cardCacheService.put(key, response);
                return response;
            });
    }
}
