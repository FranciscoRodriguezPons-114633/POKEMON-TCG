package com.utn.pokemontcg.game.domain.model;

public class TurnFlags {
    private boolean energyAttached;
    private boolean retreated;
    private boolean supporterPlayed;
    private boolean attacked;

    public boolean energyAttached() { return energyAttached; }
    public void setEnergyAttached(boolean energyAttached) { this.energyAttached = energyAttached; }
    public boolean retreated() { return retreated; }
    public void setRetreated(boolean retreated) { this.retreated = retreated; }
    public boolean supporterPlayed() { return supporterPlayed; }
    public void setSupporterPlayed(boolean supporterPlayed) { this.supporterPlayed = supporterPlayed; }
    public boolean attacked() { return attacked; }
    public void setAttacked(boolean attacked) { this.attacked = attacked; }

    public void reset() {
        this.energyAttached = false;
        this.retreated = false;
        this.supporterPlayed = false;
        this.attacked = false;
    }
}
