package com.utn.pokemontcg.game.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record SetupGameRequest(
    @Min(60) @Max(60) Integer playerOneDeckSize,
    @Min(1) @Max(60) Integer playerOneBasicCount,
    @Min(60) @Max(60) Integer playerTwoDeckSize,
    @Min(1) @Max(60) Integer playerTwoBasicCount,
    UUID playerOneDeckId,
    UUID playerTwoDeckId
) {}
