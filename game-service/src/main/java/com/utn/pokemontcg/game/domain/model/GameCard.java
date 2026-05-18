package com.utn.pokemontcg.game.domain.model;

import java.util.Set;

public record GameCard(
    String id,
    String name,
    String supertype,
    Set<String> subtypes,
    int hp,
    int attackDamage,
    int attackRequiredEnergy
) {
    public boolean isPokemon() {
        return "Pokemon".equalsIgnoreCase(supertype);
    }

    public boolean isBasicPokemon() {
        return isPokemon() && subtypes.contains("Basic");
    }

    public boolean isPokemonEx() {
        return isPokemon() && (name.endsWith("-EX") || subtypes.contains("EX"));
    }

    public boolean isBasicEnergy() {
        return "Energy".equalsIgnoreCase(supertype) && subtypes.contains("Basic");
    }
}
