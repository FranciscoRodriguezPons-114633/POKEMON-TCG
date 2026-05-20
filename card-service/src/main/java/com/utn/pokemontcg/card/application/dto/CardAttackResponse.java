package com.utn.pokemontcg.card.application.dto;

import java.util.List;

public record CardAttackResponse(
    String name,
    String text,
    String damage,
    List<String> cost,
    int convertedEnergyCost
) {
}
