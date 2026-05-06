package com.utn.pokemontcg.game.domain.chain;

public class PostDamageEffectsStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        context.setPostEffectsApplied(true);
    }
}
