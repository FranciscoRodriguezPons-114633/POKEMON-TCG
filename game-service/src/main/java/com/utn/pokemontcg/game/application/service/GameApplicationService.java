package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class GameApplicationService {

    private final GameStateRepository gameStateRepository;
    private final GameEngineFacade gameEngineFacade;
    private final SetupEngineService setupEngineService;
    private final TurnActionValidator turnActionValidator;
    private final VictoryService victoryService;
    private final RuleValidator ruleValidator;
    private final DamageCalculator damageCalculator;
    private final StatusEffectManager statusEffectManager;

    public GameApplicationService(GameStateRepository gameStateRepository,
                                  GameEngineFacade gameEngineFacade,
                                  SetupEngineService setupEngineService,
                                  TurnActionValidator turnActionValidator,
                                  VictoryService victoryService,
                                  RuleValidator ruleValidator,
                                  DamageCalculator damageCalculator,
                                  StatusEffectManager statusEffectManager) {
        this.gameStateRepository = gameStateRepository;
        this.gameEngineFacade = gameEngineFacade;
        this.setupEngineService = setupEngineService;
        this.turnActionValidator = turnActionValidator;
        this.victoryService = victoryService;
        this.ruleValidator = ruleValidator;
        this.damageCalculator = damageCalculator;
        this.statusEffectManager = statusEffectManager;
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
        initializeBoardState(game, playerOneDeckSize, playerTwoDeckSize);

        gameEngineFacade.startSetup(game);
        gameEngineFacade.startActive(game);
        GameAggregate saved = gameStateRepository.save(game);
        gameStateRepository.appendActionLog(game.id(), GameActionType.DRAW, game.currentTurnPlayer(), "SETUP_COMPLETED", Instant.now());
        return saved;
    }

    public GameAggregate executeAction(UUID gameId, GameActionType actionType) {
        GameAggregate game = get(gameId);
        turnActionValidator.validate(game, actionType);
        ruleValidator.validate(game, actionType);

        switch (actionType) {
            case DRAW -> gameEngineFacade.advanceTurnPhase(game);
            case ATTACH_ENERGY -> game.turnFlags().setEnergyAttached(true);
            case PLAY_SUPPORTER -> game.turnFlags().setSupporterPlayed(true);
            case RETREAT -> game.turnFlags().setRetreated(true);
            case ATTACK -> {
                game.turnFlags().setAttacked(true);
                gameEngineFacade.resolveAttack(game);
                UUID attacker = game.currentTurnPlayer();
                UUID defender = opponentOf(game, attacker);
                int damage = damageCalculator.calculate(
                    30,
                    false,
                    false,
                    game.statusByPlayer().getOrDefault(attacker, EnumSet.noneOf(StatusCondition.class))
                );
                game.activeHp().put(defender, game.activeHp().getOrDefault(defender, 120) - damage);
                victoryService.applyKnockoutAndPrizes(game, attacker, defender);
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
            case TAKE_PRIZE -> {
                UUID player = game.currentTurnPlayer();
                game.prizeCardsRemaining().put(player, game.prizeCardsRemaining().getOrDefault(player, 6) - 1);
            }
            case APPLY_SPECIAL_CONDITION -> statusEffectManager.applyCondition(
                game,
                opponentOf(game, game.currentTurnPlayer()),
                StatusCondition.POISONED
            );
            case RESOLVE_BETWEEN_TURNS -> statusEffectManager.resolveBetweenTurns(game, opponentOf(game, game.currentTurnPlayer()));
        }

        victoryService.closeGameIfNeeded(game);
        gameStateRepository.appendActionLog(game.id(), actionType, game.currentTurnPlayer(),
            game.gameState().name() + "/" + game.turnPhase().name(), Instant.now());

        return gameStateRepository.save(game);
    }

    public List<String> actionLog(UUID gameId) {
        return gameStateRepository.actionLog(gameId);
    }

    public GameAggregate get(UUID gameId) {
        return gameStateRepository.latestSnapshot(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }

    private void initializeBoardState(GameAggregate game, int p1DeckSize, int p2DeckSize) {
        game.prizeCardsRemaining().put(game.playerOne(), 6);
        game.prizeCardsRemaining().put(game.playerTwo(), 6);
        game.activeHp().put(game.playerOne(), 120);
        game.activeHp().put(game.playerTwo(), 120);
        game.activePokemonEx().put(game.playerOne(), false);
        game.activePokemonEx().put(game.playerTwo(), false);
        game.deckCardsRemaining().put(game.playerOne(), Math.max(0, p1DeckSize - 7 - 6));
        game.deckCardsRemaining().put(game.playerTwo(), Math.max(0, p2DeckSize - 7 - 6));
    }

    private UUID opponentOf(GameAggregate game, UUID player) {
        if (player == null) return game.playerTwo();
        return player.equals(game.playerOne()) ? game.playerTwo() : game.playerOne();
    }
}
