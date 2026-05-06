package com.utn.pokemontcg.domain.model;

import java.util.UUID;

public record PlayerSlot(UUID playerId, UUID deckId) {
}
