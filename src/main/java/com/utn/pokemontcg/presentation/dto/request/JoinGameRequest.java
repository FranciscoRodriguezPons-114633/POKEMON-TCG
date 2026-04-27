package com.utn.pokemontcg.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinGameRequest(
    @NotNull UUID playerId,
    @NotNull UUID deckId
) {}
