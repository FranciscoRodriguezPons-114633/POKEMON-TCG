package com.utn.pokemontcg.game.domain.chain;

public class SelectionStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        if (context.cancelled()) return;
        context.setTargetSelected(true);
    }
}
