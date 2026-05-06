package com.utn.pokemontcg.game.domain.chain;

public class SelectionStep implements AttackStep {
    @Override
    public void execute(AttackContext context) {
        context.audit("SelectionStep:start");
        if (context.cancelled()) return;
        context.setTargetSelected(true);
        context.audit("SelectionStep:target_selected");
    }
}
