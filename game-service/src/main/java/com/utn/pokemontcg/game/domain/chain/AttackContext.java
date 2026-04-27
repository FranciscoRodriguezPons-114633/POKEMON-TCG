package com.utn.pokemontcg.game.domain.chain;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public class AttackContext {
    private final GameAggregate game;
    private int damage;

    public AttackContext(GameAggregate game) {
        this.game = game;
    }

    public GameAggregate game() { return game; }
    public int damage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
}
