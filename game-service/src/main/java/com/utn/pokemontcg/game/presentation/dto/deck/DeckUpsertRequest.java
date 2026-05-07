package com.utn.pokemontcg.game.presentation.dto.deck;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeckUpsertRequest(
    @NotNull UUID playerId,
    @NotBlank String name,
    @Valid @NotNull List<DeckCardInput> cards
) {
}
