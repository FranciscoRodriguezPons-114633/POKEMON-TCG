package com.utn.pokemontcg.game.presentation.dto;

import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import jakarta.validation.constraints.NotNull;

public record GameActionRequest(
    @NotNull GameActionType actionType,
    StatusCondition condition
) {}
