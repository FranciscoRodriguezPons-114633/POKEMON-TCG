package com.utn.pokemontcg.game.domain.chain;

public class ModifierStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        if (context.weaknessMultiplier() < 1) context.setWeaknessMultiplier(1);
        if (context.resistanceReduction() < 0) context.setResistanceReduction(0);
    }
}
