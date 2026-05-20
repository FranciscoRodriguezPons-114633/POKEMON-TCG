package com.utn.pokemontcg.card.application;

import com.utn.pokemontcg.card.application.dto.CardSearchResponse;
import com.utn.pokemontcg.card.application.dto.CardSummaryResponse;
import com.utn.pokemontcg.card.infrastructure.PokemonTcgClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CardApiServiceTest {

    private PokemonTcgClient client;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private CardApiService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        client = mock(PokemonTcgClient.class);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new CardApiService(client, redisTemplate, new CardNormalizer());
    }

    @Test
    void normalizesSearchResponsesForGameUse() {
        when(client.search("set.id:xy1", 20)).thenReturn(searchPayload());

        CardSearchResponse response = service.search("set.id:xy1", 20);

        assertEquals(1, response.count());
        assertEquals(1, response.cards().size());
        CardSummaryResponse card = response.cards().getFirst();
        assertEquals("xy1-1", card.id());
        assertEquals("Venusaur-EX", card.name());
        assertEquals("xy1", card.setId());
        assertEquals("Pokemon", card.supertype());
        assertEquals(List.of("Basic", "EX"), card.subtypes());
        assertEquals(List.of("Grass"), card.types());
        assertEquals(180, card.hp());
        assertEquals("Poison Powder", card.attacks().getFirst().name());
        assertEquals(3, card.attacks().getFirst().convertedEnergyCost());
        assertEquals("Fire", card.weaknesses().getFirst().type());
        verify(valueOperations).set(eq("cards:search:set.id:xy1:20"), any(CardSearchResponse.class), any());
    }

    @Test
    void returnsCachedCardByIdWithoutCallingProvider() {
        CardSummaryResponse cached = new CardSummaryResponse(
            "xy1-1",
            "Venusaur-EX",
            "xy1",
            "Pokemon",
            List.of("Basic"),
            List.of("Grass"),
            180,
            List.of(),
            List.of(),
            List.of()
        );
        when(valueOperations.get("cards:id:xy1-1")).thenReturn(cached);

        CardSummaryResponse response = service.byId("xy1-1");

        assertEquals(cached, response);
        verifyNoInteractions(client);
    }

    @Test
    void stillCallsProviderWhenRedisIsUnavailable() {
        when(redisTemplate.opsForValue()).thenThrow(new IllegalStateException("redis down"));
        when(client.getById("xy1-1")).thenReturn(byIdPayload());

        CardSummaryResponse response = service.byId("xy1-1");

        assertEquals("xy1-1", response.id());
        assertEquals("Venusaur-EX", response.name());
    }

    private Map<String, Object> searchPayload() {
        return Map.of(
            "count", 1,
            "totalCount", 1,
            "data", List.of(cardPayload())
        );
    }

    private Map<String, Object> byIdPayload() {
        return Map.of("data", cardPayload());
    }

    private Map<String, Object> cardPayload() {
        return Map.of(
            "id", "xy1-1",
            "name", "Venusaur-EX",
            "set", Map.of("id", "xy1"),
            "supertype", "Pokemon",
            "subtypes", List.of("Basic", "EX"),
            "types", List.of("Grass"),
            "hp", "180",
            "attacks", List.of(Map.of(
                "name", "Poison Powder",
                "text", "Your opponent's Active Pokemon is now Poisoned.",
                "damage", "60",
                "cost", List.of("Grass", "Colorless", "Colorless"),
                "convertedEnergyCost", 3
            )),
            "weaknesses", List.of(Map.of("type", "Fire", "value", "x2")),
            "resistances", List.of(Map.of("type", "Water", "value", "-20"))
        );
    }
}
