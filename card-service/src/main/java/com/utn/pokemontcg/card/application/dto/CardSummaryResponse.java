package com.utn.pokemontcg.card.application.dto;

import java.util.List;

public record CardSummaryResponse(
    String id,
    String name,
    String setId,
    String supertype,
    List<String> subtypes,
    List<String> types,
    Integer hp,
    List<CardAttackResponse> attacks,
    List<TypeModifierResponse> weaknesses,
    List<TypeModifierResponse> resistances
) {
}
