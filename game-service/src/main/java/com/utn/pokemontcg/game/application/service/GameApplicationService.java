package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameApplicationService {

    private final GameStateRepository gameStateRepository;
    private final GameEngineFacade gameEngineFacade;
    private final SetupEngineService setupEngineService;
    private final TurnActionValidator turnActionValidator;

    public GameApplicationService(GameStateRepository gameStateRepository, GameEngineFacade gameEngineFacade, SetupEngineService setupEngineService, TurnActionValidator turnActionValidator) {
        this.gameStateRepository = gameStateRepository;
        this.gameEngineFacade = gameEngineFacade;
        this.setupEngineService = setupEngineService;
        this.turnActionValidator = turnActionValidator;
    }

    public GameAggregate create(UUID playerId) {
        GameAggregate game = new GameAggregate(playerId);
        gameStateRepository.save(game);
        return game;
    }

    public GameAggregate join(UUID gameId, UUID playerId) {
        GameAggregate game = get(gameId);
        game.setPlayerTwo(playerId);
        return gameStateRepository.save(game);
    }


    public GameAggregate runInitialSetup(UUID gameId, int playerOneDeckSize, int playerOneBasicCount, int playerTwoDeckSize, int playerTwoBasicCount) {
        GameAggregate game = get(gameId);
        if (game.playerTwo() == null) {
            throw new IllegalStateException("Two players are required before setup");
        }

        var p1 = setupEngineService.preparePlayer(playerOneDeckSize, playerOneBasicCount, 0);
        var p2 = setupEngineService.preparePlayer(playerTwoDeckSize, playerTwoBasicCount, p1.mulligans());

        p1.setHandSize(7 + p2.mulligans());

        game.setupByPlayer().put(game.playerOne(), p1);
        game.setupByPlayer().put(game.playerTwo(), p2);

        game.setFirstPlayer(Math.random() < 0.5 ? game.playerOne() : game.playerTwo());
        game.setCurrentTurnPlayer(game.firstPlayer());

        gameEngineFacade.startSetup(game);
        gameEngineFacade.startActive(game);
        return gameStateRepository.save(game);
    }

    public GameAggregate executeAction(UUID gameId, GameActionType actionType) {
        GameAggregate game = get(gameId);
        turnActionValidator.validate(game, actionType);

        switch (actionType) {
            case DRAW -> gameEngineFacade.advanceTurnPhase(game);
            case ATTACH_ENERGY -> game.turnFlags().setEnergyAttached(true);
            case PLAY_SUPPORTER -> game.turnFlags().setSupporterPlayed(true);
            case RETREAT -> game.turnFlags().setRetreated(true);
            case ATTACK -> {
                game.turnFlags().setAttacked(true);
                gameEngineFacade.resolveAttack(game);
            }
            case END_TURN -> {
                gameEngineFacade.advanceTurnPhase(game);
                if (game.turnPhase() == TurnPhase.DRAW) {
                    game.setCurrentTurnPlayer(game.currentTurnPlayer() != null && game.currentTurnPlayer().equals(game.playerOne())
                        ? game.playerTwo() : game.playerOne());
                    game.turnFlags().reset();
                    game.setFirstTurn(false);
                }
            }
        }

        return gameStateRepository.save(game);
    }

    public GameAggregate get(UUID gameId) {
        return gameStateRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
}
