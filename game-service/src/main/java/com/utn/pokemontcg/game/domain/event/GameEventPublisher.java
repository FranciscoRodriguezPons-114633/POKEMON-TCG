package com.utn.pokemontcg.game.domain.event;

import java.util.ArrayList;
import java.util.List;

public class GameEventPublisher {

    private final List<GameEventObserver> observers = new ArrayList<>();

    public void subscribe(GameEventObserver observer) {
        observers.add(observer);
    }

    public void publish(GameEvent event) {
        observers.forEach(observer -> observer.onEvent(event));
    }
}
