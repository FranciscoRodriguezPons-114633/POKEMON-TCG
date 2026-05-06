package com.utn.pokemontcg.domain.engine;

import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.domain.model.GameState;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameEngineFacade {

    public void startGame(Game game) {
        if (game.getPlayers().size() != 2 || game.getState() != GameState.SETUP) {
            throw new IllegalStateException("La partida debe tener 2 jugadores y estar en SETUP");
        }
        UUID firstPlayer = game.getPlayers().get(0).playerId();
        game.activate(firstPlayer);
    }

    public void resolveAttack(Game game) {
        if (game.getState() != GameState.ACTIVE) {
            throw new IllegalStateException("Solo se puede atacar durante una partida activa");
        }
        game.nextPhase();
    }
}
