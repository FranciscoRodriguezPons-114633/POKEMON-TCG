package com.utn.pokemontcg.game.domain.model;

public class PlayerSetupState {
    private int mulligans;
    private int handSize;
    private int benchCount;
    private int prizeCount;
    private boolean hasActive;

    public int mulligans() { return mulligans; }
    public void setMulligans(int mulligans) { this.mulligans = mulligans; }
    public int handSize() { return handSize; }
    public void setHandSize(int handSize) { this.handSize = handSize; }
    public int benchCount() { return benchCount; }
    public void setBenchCount(int benchCount) { this.benchCount = benchCount; }
    public int prizeCount() { return prizeCount; }
    public void setPrizeCount(int prizeCount) { this.prizeCount = prizeCount; }
    public boolean hasActive() { return hasActive; }
    public void setHasActive(boolean hasActive) { this.hasActive = hasActive; }
}
