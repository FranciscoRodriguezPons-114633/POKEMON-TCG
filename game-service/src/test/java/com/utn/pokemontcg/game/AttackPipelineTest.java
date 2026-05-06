package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.domain.chain.*;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackPipelineTest {

    @Test
    void shouldCancelAttackWhenEnergyIsInsufficient() {
        AttackContext context = new AttackContext(new GameAggregate(UUID.randomUUID()));
        context.setRequiredEnergy(2);
        context.setAttachedEnergy(1);
        context.setBaseDamage(50);

        new AttackResolutionPipeline(List.of(
            new EnergyValidationStep(),
            new DamageCalculationStep()
        )).resolve(context);

        assertTrue(context.cancelled());
        assertEquals(0, context.damage());
    }

    @Test
    void shouldCalculateDamageWithWeaknessAndResistance() {
        AttackContext context = new AttackContext(new GameAggregate(UUID.randomUUID()));
        context.setRequiredEnergy(1);
        context.setAttachedEnergy(1);
        context.setBaseDamage(40);
        context.setWeaknessMultiplier(2);
        context.setResistanceReduction(20);

        new AttackResolutionPipeline(List.of(
            new EnergyValidationStep(),
            new ModifierStep(),
            new DamageCalculationStep(),
            new PostDamageEffectsStep()
        )).resolve(context);

        assertEquals(60, context.damage());
        assertTrue(context.postEffectsApplied());
    }
}
