package com.utn.pokemontcg.domain;

import com.utn.pokemontcg.domain.validator.DeckRulesValidator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeckRulesValidatorTest {

    private final DeckRulesValidator validator = new DeckRulesValidator();

    @Test
    void shouldValidateLegalDeck() {
        Map<String, Integer> cards = new HashMap<>();
        for (int i = 0; i < 15; i++) {
            cards.put("xy1-" + i, 4);
        }

        assertDoesNotThrow(() -> validator.validateDeck(cards, false, true));
    }

    @Test
    void shouldFailWhenDeckHasNot60Cards() {
        Map<String, Integer> cards = Map.of("xy1-1", 4);

        assertThrows(IllegalArgumentException.class,
            () -> validator.validateDeck(cards, false, true));
    }
}
