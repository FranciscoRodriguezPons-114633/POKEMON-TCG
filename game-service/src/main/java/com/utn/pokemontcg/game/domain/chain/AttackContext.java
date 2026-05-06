package com.utn.pokemontcg.game.domain.chain;

import com.utn.pokemontcg.game.domain.model.GameAggregate;

public class AttackContext {
    private final GameAggregate game;
    private int requiredEnergy;
    private int attachedEnergy;
    private boolean confused;
    private boolean cancelled;
    private boolean targetSelected;
    private int baseDamage;
    private int preAttackBonus;
    private int weaknessMultiplier = 1;
    private int resistanceReduction;
    private int damage;
    private int selfDamage;
    private boolean postEffectsApplied;

    public AttackContext(GameAggregate game) {
        this.game = game;
    }

    public GameAggregate game() { return game; }
    public int requiredEnergy() { return requiredEnergy; }
    public void setRequiredEnergy(int requiredEnergy) { this.requiredEnergy = requiredEnergy; }
    public int attachedEnergy() { return attachedEnergy; }
    public void setAttachedEnergy(int attachedEnergy) { this.attachedEnergy = attachedEnergy; }
    public boolean confused() { return confused; }
    public void setConfused(boolean confused) { this.confused = confused; }
    public boolean cancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public boolean targetSelected() { return targetSelected; }
    public void setTargetSelected(boolean targetSelected) { this.targetSelected = targetSelected; }
    public int baseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }
    public int preAttackBonus() { return preAttackBonus; }
    public void setPreAttackBonus(int preAttackBonus) { this.preAttackBonus = preAttackBonus; }
    public int weaknessMultiplier() { return weaknessMultiplier; }
    public void setWeaknessMultiplier(int weaknessMultiplier) { this.weaknessMultiplier = weaknessMultiplier; }
    public int resistanceReduction() { return resistanceReduction; }
    public void setResistanceReduction(int resistanceReduction) { this.resistanceReduction = resistanceReduction; }
    public int damage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int selfDamage() { return selfDamage; }
    public void setSelfDamage(int selfDamage) { this.selfDamage = selfDamage; }
    public boolean postEffectsApplied() { return postEffectsApplied; }
    public void setPostEffectsApplied(boolean postEffectsApplied) { this.postEffectsApplied = postEffectsApplied; }
}
