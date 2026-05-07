package com.utn.pokemontcg.game.application.service.deck;

import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeckValidationService {

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

        return new DeckValidationResult(errors.isEmpty(), errors);
    }
}
