package com.utn.pokemontcg.card.application.dto;

import java.util.List;

public record CardSearchResponse(
    String query,
    int pageSize,
    int count,
    int totalCount,
    List<CardSummaryResponse> cards
) {
}
