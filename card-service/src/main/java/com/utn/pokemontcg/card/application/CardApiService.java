package com.utn.pokemontcg.card.application;

import com.utn.pokemontcg.card.infrastructure.PokemonTcgClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class CardApiService {

    private static final Duration TTL = Duration.ofDays(7);

    private final PokemonTcgClient pokemonTcgClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public CardApiService(PokemonTcgClient pokemonTcgClient, RedisTemplate<String, Object> redisTemplate) {
        this.pokemonTcgClient = pokemonTcgClient;
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> search(String query, int pageSize) {
        String key = "cards:search:" + query + ":" + pageSize;
        Object cache = redisTemplate.opsForValue().get(key);
        if (cache instanceof Map<?, ?> cachedMap) {
            return (Map<String, Object>) cachedMap;
        }
        Map<String, Object> result = pokemonTcgClient.search(query, pageSize);
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> byId(String id) {
        String key = "cards:id:" + id;
        Object cache = redisTemplate.opsForValue().get(key);
        if (cache instanceof Map<?, ?> cachedMap) {
            return (Map<String, Object>) cachedMap;
        }
        Map<String, Object> result = pokemonTcgClient.getById(id);
        redisTemplate.opsForValue().set(key, result, TTL);
        return result;
    }
}
