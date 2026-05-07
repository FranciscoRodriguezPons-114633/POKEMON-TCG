package com.utn.pokemontcg.game.presentation.dto.deck;

import java.util.List;

public record DeckValidationResult(boolean valid, List<String> errors) {
}
