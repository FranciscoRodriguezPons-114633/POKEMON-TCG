package com.utn.pokemontcg.game.domain.engine;

import org.springframework.stereotype.Component;

@Component
public class DamageCalculator {

    public int calculate(int baseDamage, boolean weakness, boolean resistance, Object ignoredAttackerStatuses) {
        int damage = baseDamage;
        if (weakness) damage *= 2;
        if (resistance) damage -= 20;

        return Math.max(0, damage);
    }
}
