package com.utn.pokemontcg.card.application;

import com.utn.pokemontcg.card.application.dto.CardSearchResponse;
import com.utn.pokemontcg.card.application.dto.CardSummaryResponse;
import com.utn.pokemontcg.card.infrastructure.PokemonTcgClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Service
public class CardApiService {

    private static final Duration TTL = Duration.ofDays(7);

    private final PokemonTcgClient pokemonTcgClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CardNormalizer normalizer;

    public CardApiService(PokemonTcgClient pokemonTcgClient,
                          RedisTemplate<String, Object> redisTemplate,
                          CardNormalizer normalizer) {
        this.pokemonTcgClient = pokemonTcgClient;
        this.redisTemplate = redisTemplate;
        this.normalizer = normalizer;
    }

    public CardSearchResponse search(@NonNull String query, int pageSize, int page) {
        int safePageSize = Math.max(1, Math.min(pageSize, 250));
        int safePage = Math.max(1, page);
        String key = "cards:search:" + query + ":" + safePageSize + ":" + safePage;
        Object cache = readCache(key);
        if (cache instanceof CardSearchResponse response) {
            return response;
        }
        Map<String, Object> result = Objects.requireNonNull(pokemonTcgClient.search(query, safePageSize, safePage));
        CardSearchResponse response = normalizer.searchResponse(query, safePageSize, result);
        writeCache(key, response);
        return response;
    }

    public CardSummaryResponse byId(@NonNull String id) {
        String key = "cards:id:" + id;
        Object cache = readCache(key);
        if (cache instanceof CardSummaryResponse response) {
            return response;
        }
        Map<String, Object> result = Objects.requireNonNull(pokemonTcgClient.getById(id));
        CardSummaryResponse response = normalizer.byIdResponse(result);
        writeCache(key, response);
        return response;
    }

    private Object readCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void writeCache(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, TTL);
        } catch (RuntimeException ignored) {
            // Card lookup must keep working even if Redis is temporarily unavailable.
        }
    }
}
