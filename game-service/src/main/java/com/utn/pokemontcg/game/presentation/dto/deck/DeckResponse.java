package com.utn.pokemontcg.game.presentation.dto.deck;

import java.util.List;
import java.util.UUID;

public record DeckResponse(UUID id, UUID playerId, String name, List<DeckCardInput> cards, DeckValidationResult validation) {
}
