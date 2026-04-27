package com.utn.pokemontcg.infrastructure.external.pokemontcg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PokemonTcgClientImpl implements PokemonTcgClient {

    private final RestClient restClient;

    public PokemonTcgClientImpl(@Value("${pokemontcg.base-url}") String baseUrl,
                                @Value("${pokemontcg.api-key:}") String apiKey) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("X-Api-Key", apiKey)
            .build();
    }

    @Override
    public Map<String, Object> searchCards(String query, int pageSize) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/cards")
                .queryParam("q", query)
                .queryParam("pageSize", pageSize)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(Map.class);
    }
}
