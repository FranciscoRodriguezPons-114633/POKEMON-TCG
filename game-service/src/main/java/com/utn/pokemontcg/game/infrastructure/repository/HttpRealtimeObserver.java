package com.utn.pokemontcg.game.infrastructure.repository;

import com.utn.pokemontcg.game.domain.event.GameEvent;
import com.utn.pokemontcg.game.domain.event.GameEventObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpRealtimeObserver implements GameEventObserver {

    private final RestClient restClient;

    public HttpRealtimeObserver(@Value("${realtime.service.url:http://localhost:8082}") @NonNull String realtimeUrl) {
        this.restClient = RestClient.builder().baseUrl(realtimeUrl).build();
    }

    @Override
    public void onEvent(@NonNull GameEvent event) {
        restClient.post()
            .uri("/internal/events")
            .contentType(MediaType.APPLICATION_JSON)
            .body(event)
            .retrieve()
            .toBodilessEntity();
    }
}
