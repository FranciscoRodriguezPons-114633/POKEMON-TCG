package com.utn.pokemontcg.application.service;

import com.utn.pokemontcg.domain.validator.DeckRulesValidator;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeckService {

    private final DeckRulesValidator deckRulesValidator;

    public DeckService(DeckRulesValidator deckRulesValidator) {
        this.deckRulesValidator = deckRulesValidator;
    }

    public void validateDeck(Map<String, Integer> cardsByApiId, boolean hasAceSpec, boolean hasBasicPokemon) {
        deckRulesValidator.validateDeck(cardsByApiId, hasAceSpec, hasBasicPokemon);
    }
}
