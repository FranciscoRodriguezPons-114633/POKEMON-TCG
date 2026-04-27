package com.utn.pokemontcg.game.domain.chain;

import java.util.List;

public class AttackResolutionPipeline {

    private final List<AttackStep> steps;

    public AttackResolutionPipeline(List<AttackStep> steps) {
        this.steps = steps;
    }

    public void resolve(AttackContext context) {
        for (AttackStep step : steps) {
            step.execute(context);
        }
    }
}
