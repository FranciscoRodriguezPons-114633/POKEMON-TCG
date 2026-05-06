package com.utn.pokemontcg.game.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinGameRequest(@NotNull UUID playerId) {}
