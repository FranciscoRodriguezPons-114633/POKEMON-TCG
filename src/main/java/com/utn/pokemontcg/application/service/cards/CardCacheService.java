package com.utn.pokemontcg.application.service.cards;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class CardCacheService {

    private static final Duration CARD_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;

    public CardCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Map<?, ?> map) {
            return Optional.of((Map<String, Object>) map);
        }
        return Optional.empty();
    }

    public void put(String key, Map<String, Object> value) {
        redisTemplate.opsForValue().set(key, value, CARD_TTL);
    }
}
