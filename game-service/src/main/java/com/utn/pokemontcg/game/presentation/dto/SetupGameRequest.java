package com.utn.pokemontcg.game.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetupGameRequest(
    @NotNull @Min(60) @Max(60) Integer playerOneDeckSize,
    @NotNull @Min(1) @Max(60) Integer playerOneBasicCount,
    @NotNull @Min(60) @Max(60) Integer playerTwoDeckSize,
    @NotNull @Min(1) @Max(60) Integer playerTwoBasicCount
) {}
