package com.utn.pokemontcg.game.domain.chain;

public class PostDamageEffectsStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        context.audit("PostDamageEffectsStep:start");
        if (context.cancelled()) return;
        context.setPostEffectsApplied(true);
        context.audit("PostDamageEffectsStep:effects_applied");
    }
}
