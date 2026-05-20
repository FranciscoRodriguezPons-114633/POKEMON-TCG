package com.utn.pokemontcg.card.infrastructure;

import com.utn.pokemontcg.card.application.CardProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

@Component
public class PokemonTcgClientImpl implements PokemonTcgClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    public PokemonTcgClientImpl(@Value("${pokemontcg.base-url}") @NonNull String baseUrl,
                                @Value("${pokemontcg.api-key:}") String apiKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }
        this.restClient = builder.build();
    }

    @Override
    public Map<String, Object> search(String query, int pageSize, int page) {
        try {
            return Objects.requireNonNull(restClient.get()
                .uri(uri -> uri.path("/cards")
                    .queryParam("q", query)
                    .queryParam("pageSize", pageSize)
                    .queryParam("page", page)
                    .build())
                .retrieve()
                .body(MAP_TYPE));
        } catch (RestClientException ex) {
            throw new CardProviderException("No se pudo consultar pokemontcg.io", ex);
        }
    }

    @Override
    public Map<String, Object> getById(String id) {
        try {
            return Objects.requireNonNull(restClient.get()
                .uri("/cards/{id}", id)
                .retrieve()
                .body(MAP_TYPE));
        } catch (RestClientException ex) {
            throw new CardProviderException("No se pudo consultar pokemontcg.io", ex);
        }
    }
}
