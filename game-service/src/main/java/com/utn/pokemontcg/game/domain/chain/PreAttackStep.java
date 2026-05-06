package com.utn.pokemontcg.game.domain.chain;

public class PreAttackStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        context.audit("PreAttackStep:start");
        if (context.cancelled()) return;
        context.setPreAttackBonus(0);
        context.audit("PreAttackStep:bonus_applied=" + context.preAttackBonus());
    }
}
