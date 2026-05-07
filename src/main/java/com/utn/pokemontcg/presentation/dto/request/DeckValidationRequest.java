package com.utn.pokemontcg.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record DeckValidationRequest(
    @NotEmpty Map<String, Integer> cardsByApiId,
    @NotNull Boolean hasAceSpec,
    @NotNull Boolean hasBasicPokemon
) {}
