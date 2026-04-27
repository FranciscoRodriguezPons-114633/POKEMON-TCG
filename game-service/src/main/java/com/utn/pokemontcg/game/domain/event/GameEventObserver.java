package com.utn.pokemontcg.game.domain.event;

public interface GameEventObserver {
    void onEvent(GameEvent event);
}
