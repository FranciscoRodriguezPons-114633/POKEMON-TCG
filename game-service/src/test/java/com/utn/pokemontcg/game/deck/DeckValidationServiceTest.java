package com.utn.pokemontcg.game.deck;

import com.utn.pokemontcg.game.application.service.deck.DeckValidationService;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckValidationServiceTest {

    private final DeckValidationService validator = new DeckValidationService();

    @Test
    void shouldRejectDeckWithout60Cards() {
        var result = validator.validate(List.of(new DeckCardInput("c1", "Card 1", 10, "Pokemon", "Basic", false, true, false)));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("60")));
    }

    @Test
    void shouldRejectMoreThanOneAceSpec() {
        var cards = List.of(
            new DeckCardInput("a1", "Ace 1", 1, "Trainer", "ACE SPEC", false, false, true),
            new DeckCardInput("a2", "Ace 2", 1, "Trainer", "ACE SPEC", false, false, true),
            new DeckCardInput("b1", "Basic", 58, "Pokemon", "Basic", false, true, false)
        );
        var result = validator.validate(cards);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("AS TÁCTICO")));
    }
}
