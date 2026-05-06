package com.utn.pokemontcg.infrastructure.external.pokemontcg;

import java.util.Map;

public interface PokemonTcgClient {
    Map<String, Object> searchCards(String query, int pageSize);
}
