package com.utn.pokemontcg.realtime.application;

public class InvalidGameEventException extends RuntimeException {

    public InvalidGameEventException(String message) {
        super(message);
    }
}
