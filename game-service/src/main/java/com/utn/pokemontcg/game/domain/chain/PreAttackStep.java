package com.utn.pokemontcg.game.domain.chain;

public class PreAttackStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        context.setPreAttackBonus(0);
    }
}
