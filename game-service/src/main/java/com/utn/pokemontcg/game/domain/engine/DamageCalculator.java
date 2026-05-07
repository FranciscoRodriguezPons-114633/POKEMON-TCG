package com.utn.pokemontcg.game.domain.engine;

import com.utn.pokemontcg.game.domain.model.StatusCondition;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class DamageCalculator {

    public int calculate(int baseDamage, boolean weakness, boolean resistance, EnumSet<StatusCondition> attackerStatuses) {
        int damage = baseDamage;
        if (weakness) damage *= 2;
        if (resistance) damage -= 20;

        if (attackerStatuses != null && attackerStatuses.contains(StatusCondition.BURNED)) {
            damage += 10;
        }

        return Math.max(0, damage);
    }
}
