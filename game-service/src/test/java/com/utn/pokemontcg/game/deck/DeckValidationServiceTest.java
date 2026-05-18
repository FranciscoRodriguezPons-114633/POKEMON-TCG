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
        var result = validator.validate(List.of(new DeckCardInput("xy1-1", "Card 1", "xy1", 10, "Pokemon", "Basic", false, true, false, 120, 30, 1)));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("60")));
    }

    @Test
    void shouldRejectMoreThanOneAceSpec() {
        var cards = List.of(
            new DeckCardInput("xy1-1", "Ace 1", "xy1", 1, "Trainer", "ACE SPEC", false, false, true, null, null, null),
            new DeckCardInput("xy1-2", "Ace 2", "xy1", 1, "Trainer", "ACE SPEC", false, false, true, null, null, null),
            new DeckCardInput("xy1-3", "Basic", "xy1", 58, "Pokemon", "Basic", false, true, false, 120, 30, 1)
        );
        var result = validator.validate(cards);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("AS TÁCTICO")));
    }

    @Test
    void shouldRejectCardsOutsideXy1() {
        var cards = List.of(
            new DeckCardInput("bw1-1", "Wrong Set", "bw1", 4, "Pokemon", "Basic", false, true, false, 120, 30, 1),
            new DeckCardInput("xy1-2", "Basic", "xy1", 56, "Pokemon", "Basic", false, true, false, 120, 30, 1)
        );

        var result = validator.validate(cards);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("xy1")));
    }
}
