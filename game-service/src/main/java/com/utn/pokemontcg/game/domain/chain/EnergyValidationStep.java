package com.utn.pokemontcg.game.domain.chain;

public class EnergyValidationStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        context.audit("EnergyValidationStep:start");
        if (context.cancelled()) return;
        if (context.attachedEnergy() < context.requiredEnergy()) {
            context.setCancelled(true);
            context.audit("EnergyValidationStep:cancelled_insufficient_energy");
            return;
        }
        context.audit("EnergyValidationStep:ok");
    }
}
