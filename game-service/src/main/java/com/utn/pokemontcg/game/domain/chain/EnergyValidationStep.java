package com.utn.pokemontcg.game.domain.chain;

public class EnergyValidationStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        if (context.attachedEnergy() < context.requiredEnergy()) {
            context.setCancelled(true);
        }
    }
}
