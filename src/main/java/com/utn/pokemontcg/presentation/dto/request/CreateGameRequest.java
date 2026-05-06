package com.utn.pokemontcg.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGameRequest(
    @NotNull UUID playerId,
    @NotNull UUID deckId
) {}
