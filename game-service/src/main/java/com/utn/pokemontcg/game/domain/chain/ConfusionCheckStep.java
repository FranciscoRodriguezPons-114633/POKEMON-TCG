package com.utn.pokemontcg.game.domain.chain;

import java.security.SecureRandom;

public class ConfusionCheckStep implements AttackStep {
    private final SecureRandom random = new SecureRandom();

    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        if (!context.confused()) return;

        boolean heads = random.nextBoolean();
        if (!heads) {
            context.setCancelled(true);
            context.setSelfDamage(30);
        }
    }
}
