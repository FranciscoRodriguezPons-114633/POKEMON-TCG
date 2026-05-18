package com.utn.pokemontcg.game.presentation.dto.deck;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeckCardInput(
    @NotBlank String cardId,
    @NotBlank String name,
    String setId,
    @Min(1) int quantity,
    @NotBlank String type,
    String subtype,
    @NotNull Boolean basicEnergy,
    @NotNull Boolean basicPokemon,
    @NotNull Boolean aceSpec,
    Integer hp,
    Integer attackDamage,
    Integer attackRequiredEnergy
) {
}
