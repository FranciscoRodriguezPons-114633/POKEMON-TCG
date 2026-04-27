package com.utn.pokemontcg.application.service;

import com.utn.pokemontcg.domain.engine.GameEngineFacade;
import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.domain.model.TurnPhase;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<UUID, Game> inMemoryGames = new ConcurrentHashMap<>();
    private final GameEngineFacade engineFacade;

    public GameService(GameEngineFacade engineFacade) {
        this.engineFacade = engineFacade;
    }

    public Game createGame(UUID playerId, UUID deckId) {
        Game game = new Game(playerId, deckId);
        inMemoryGames.put(game.getId(), game);
        return game;
    }

    public Game joinGame(UUID gameId, UUID playerId, UUID deckId) {
        Game game = getById(gameId);
        game.join(playerId, deckId);
        engineFacade.startGame(game);
        return game;
    }

    public Game getById(UUID gameId) {
        Game game = inMemoryGames.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }
        return game;
    }

    public Game resolveAttack(UUID gameId) {
        Game game = getById(gameId);
        engineFacade.resolveAttack(game);
        return game;
    }

    public Game endMainPhase(UUID gameId) {
        Game game = getById(gameId);
        if (game.getPhase() != TurnPhase.MAIN) {
            throw new IllegalStateException("Solo podés cerrar la fase MAIN cuando la partida está en MAIN");
        }
        game.startAttackPhase();
        return game;
    }

    public Game nextTurn(UUID gameId) {
        Game game = getById(gameId);
        engineFacade.processBetweenTurns(game);
        return game;
    }
}
