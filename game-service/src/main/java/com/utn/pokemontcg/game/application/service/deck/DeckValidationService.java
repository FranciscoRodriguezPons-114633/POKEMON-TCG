package com.utn.pokemontcg.game.application.service.deck;

import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeckValidationService {

    private static final String REQUIRED_SET = "xy1";

    public DeckValidationResult validate(List<DeckCardInput> cards) {
        List<String> errors = new ArrayList<>();

        int total = cards.stream().mapToInt(DeckCardInput::quantity).sum();
        if (total != 60) {
            errors.add("El mazo debe tener exactamente 60 cartas.");
        }

        long aceSpecs = cards.stream().filter(DeckCardInput::aceSpec).mapToLong(DeckCardInput::quantity).sum();
        if (aceSpecs > 1) {
            errors.add("Solo se permite 1 carta AS TÁCTICO en todo el mazo.");
        }

        boolean hasBasicPokemon = cards.stream().anyMatch(DeckCardInput::basicPokemon);
        if (!hasBasicPokemon) {
            errors.add("El mazo debe incluir al menos 1 Pokémon Básico.");
        }

        cards.stream()
            .filter(c -> !c.basicEnergy() && c.quantity() > 4)
            .forEach(c -> errors.add("La carta '" + c.name() + "' excede el máximo de 4 copias."));

        cards.stream()
            .filter(c -> !REQUIRED_SET.equalsIgnoreCase(setIdOf(c)))
            .forEach(c -> errors.add("La carta '" + c.name() + "' no pertenece al set obligatorio xy1."));

        return new DeckValidationResult(errors.isEmpty(), errors);
    }

    private String setIdOf(DeckCardInput card) {
        if (card.setId() != null && !card.setId().isBlank()) {
            return card.setId();
        }
        int separator = card.cardId().indexOf('-');
        return separator > 0 ? card.cardId().substring(0, separator) : "";
    }
}
