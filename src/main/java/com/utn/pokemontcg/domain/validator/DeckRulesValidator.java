package com.utn.pokemontcg.domain.validator;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeckRulesValidator {

    public void validateDeck(Map<String, Integer> cardsByApiId, boolean hasAceSpec, boolean hasBasicPokemon) {
        int totalCards = cardsByApiId.values().stream().mapToInt(Integer::intValue).sum();
        if (totalCards != 60) throw new IllegalArgumentException("Un mazo debe tener exactamente 60 cartas");

        boolean hasIllegalDuplicates = cardsByApiId.values().stream().anyMatch(qty -> qty > 4);
        if (hasIllegalDuplicates) throw new IllegalArgumentException("Máximo 4 copias por carta");

        if (!hasBasicPokemon) throw new IllegalArgumentException("El mazo debe contener al menos 1 Pokémon Básico");

        if (!hasAceSpec) {
            return;
        }

        long aceSpecCount = cardsByApiId.keySet().stream()
            .filter(cardId -> cardId.toLowerCase().contains("acespec"))
            .count();

        if (aceSpecCount > 1) throw new IllegalArgumentException("Solo se permite 1 AS TÁCTICO por mazo");
    }
}
