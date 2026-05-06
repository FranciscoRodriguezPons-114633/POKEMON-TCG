package com.utn.pokemontcg.card.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PokemonTcgClientImpl implements PokemonTcgClient {

    private final RestClient restClient;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    public PokemonTcgClientImpl(@Value("${pokemontcg.base-url}") String baseUrl,
            @Value("${pokemontcg.api-key:}") String apiKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }
        this.restClient = builder.build();
    }

    @Override
    public Map<String, Object> search(String query, int pageSize) {
        return restClient.get()
                .uri(uri -> uri.path("/cards").queryParam("q", query).queryParam("pageSize", pageSize).build())
                .retrieve()
                .body(MAP_TYPE);
    }

    @Override
    public Map<String, Object> getById(String id) {
        return restClient.get().uri("/cards/{id}", id).retrieve().body(MAP_TYPE);
    }
}