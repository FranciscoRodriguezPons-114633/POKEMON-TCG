package com.utn.pokemontcg.game.domain.chain;

public class DamageCalculationStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        context.audit("DamageCalculationStep:start");
        if (context.cancelled()) {
            context.setDamage(0);
            context.audit("DamageCalculationStep:cancelled_damage=0");
            return;
        }

        int raw = (context.baseDamage() + context.preAttackBonus()) * context.weaknessMultiplier() - context.resistanceReduction();
        context.setDamage(Math.max(raw, 0));
        context.audit("DamageCalculationStep:damage=" + context.damage());
    }
}
