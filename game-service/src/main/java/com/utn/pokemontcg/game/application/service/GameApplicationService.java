package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameApplicationService {

    private final GameStateRepository gameStateRepository;
    private final GameEngineFacade gameEngineFacade;

    public GameApplicationService(GameStateRepository gameStateRepository, GameEngineFacade gameEngineFacade) {
        this.gameStateRepository = gameStateRepository;
        this.gameEngineFacade = gameEngineFacade;
    }

    public GameAggregate create(UUID playerId) {
        GameAggregate game = new GameAggregate(playerId);
        gameStateRepository.save(game);
        return game;
    }

    public GameAggregate join(UUID gameId, UUID playerId) {
        GameAggregate game = get(gameId);
        game.setPlayerTwo(playerId);
        gameEngineFacade.startSetup(game);
        gameEngineFacade.startActive(game);
        game.setCurrentTurnPlayer(game.playerOne());
        return gameStateRepository.save(game);
    }

    public GameAggregate executeAction(UUID gameId, String actionType) {
        GameAggregate game = get(gameId);
        if ("ATTACK".equals(actionType)) {
            gameEngineFacade.resolveAttack(game);
        } else {
            gameEngineFacade.advanceTurnPhase(game);
        }

        if (game.turnPhase() == TurnPhase.BETWEEN_TURNS) {
            game.setCurrentTurnPlayer(game.currentTurnPlayer() != null && game.currentTurnPlayer().equals(game.playerOne())
                ? game.playerTwo() : game.playerOne());
        }
        return gameStateRepository.save(game);
    }

    public GameAggregate get(UUID gameId) {
        return gameStateRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
}
