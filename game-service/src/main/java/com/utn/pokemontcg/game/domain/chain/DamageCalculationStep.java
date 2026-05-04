package com.utn.pokemontcg.game.domain.chain;

public class DamageCalculationStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) {
            context.setDamage(0);
            return;
        }

        int raw = (context.baseDamage() + context.preAttackBonus()) * context.weaknessMultiplier() - context.resistanceReduction();
        context.setDamage(Math.max(raw, 0));
    }
}
