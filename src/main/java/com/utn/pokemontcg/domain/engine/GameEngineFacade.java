package com.utn.pokemontcg.domain.engine;

import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.domain.model.GameState;
import com.utn.pokemontcg.domain.model.TurnPhase;
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
        game.startMainPhase();
    }

    public void resolveAttack(Game game) {
        if (game.getState() != GameState.ACTIVE) {
            throw new IllegalStateException("Solo se puede atacar durante una partida activa");
        }
        if (game.getPhase() != TurnPhase.ATTACK) {
            throw new IllegalStateException("Solo se puede atacar durante la fase ATTACK");
        }
        game.startBetweenTurns();
    }

    public void processBetweenTurns(Game game) {
        if (game.getPhase() != TurnPhase.BETWEEN_TURNS) {
            throw new IllegalStateException("La partida debe estar en BETWEEN_TURNS");
        }
        game.swapTurnPlayer();
        game.startDrawPhase();
        game.startMainPhase();
    }
}
