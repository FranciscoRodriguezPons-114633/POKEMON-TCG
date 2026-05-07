package com.utn.pokemontcg.game.domain.chain;

public interface AttackStep {
    void execute(AttackContext context);
}
