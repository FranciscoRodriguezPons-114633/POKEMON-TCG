package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.chain.AttackContext;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.event.GameEventType;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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

    public GameApplicationService(
        GameStateRepository gameStateRepository,
        GameEngineFacade gameEngineFacade,
        SetupEngineService setupEngineService,
        TurnActionValidator turnActionValidator,
        VictoryService victoryService,
        RuleValidator ruleValidator,
        DamageCalculator damageCalculator,
        StatusEffectManager statusEffectManager
    ) {
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
        GameAggregate saved = gameStateRepository.save(game);
        gameEngineFacade.publish(saved, GameEventType.GAME_CREATED, Map.of("playerOne", playerId));
        return saved;
    }

    public GameAggregate join(UUID gameId, UUID playerId) {
        GameAggregate game = get(gameId);
        game.setPlayerTwo(playerId);
        GameAggregate saved = gameStateRepository.save(game);
        gameEngineFacade.publish(saved, GameEventType.PLAYER_JOINED, Map.of("playerTwo", playerId));
        return saved;
    }

    public GameAggregate runInitialSetup(
        UUID gameId,
        int playerOneDeckSize,
        int playerOneBasicCount,
        int playerTwoDeckSize,
        int playerTwoBasicCount
    ) {
        GameAggregate game = get(gameId);

        if (game.playerTwo() == null) {
            throw new IllegalStateException("Two players are required before setup");
        }

        var p1 = setupEngineService.preparePlayerBoard(
            game.playerOne(),
            playerOneDeckSize,
            playerOneBasicCount,
            0,
            game
        );

        var p2 = setupEngineService.preparePlayerBoard(
            game.playerTwo(),
            playerTwoDeckSize,
            playerTwoBasicCount,
            p1.mulligans(),
            game
        );

        drawOptionalMulliganCards(game, game.playerOne(), p2.mulligans());
        p1.setHandSize(game.hand().get(game.playerOne()).size());

        game.setupByPlayer().put(game.playerOne(), p1);
        game.setupByPlayer().put(game.playerTwo(), p2);

        game.setFirstPlayer(
            Math.random() < 0.5
                ? game.playerOne()
                : game.playerTwo()
        );

        game.setCurrentTurnPlayer(game.firstPlayer());

        gameEngineFacade.startSetup(game);
        gameEngineFacade.startActive(game);

        GameAggregate saved = gameStateRepository.save(game);

        gameStateRepository.appendActionLog(
            game.id(),
            GameActionType.DRAW,
            game.currentTurnPlayer(),
            "SETUP_COMPLETED",
            Instant.now()
        );

        gameEngineFacade.publish(saved, GameEventType.SETUP_COMPLETED, Map.of(
            "firstPlayer", saved.firstPlayer(),
            "currentTurnPlayer", saved.currentTurnPlayer(),
            "phase", saved.turnPhase().name()
        ));

        return saved;
    }

    public GameAggregate executeAction(UUID gameId, GameActionType actionType) {
        return executeAction(gameId, actionType, StatusCondition.POISONED);
    }

    public GameAggregate executeAction(UUID gameId, GameActionType actionType, StatusCondition condition) {
        GameAggregate game = get(gameId);
        UUID actingPlayer = game.currentTurnPlayer();
        StatusCondition conditionToApply = condition == null ? StatusCondition.POISONED : condition;

        turnActionValidator.validate(game, actionType);
        ruleValidator.validate(game, actionType);

        switch (actionType) {
            case DRAW -> {
                drawForTurn(game, game.currentTurnPlayer());
                gameEngineFacade.advanceTurnPhase(game);
                gameEngineFacade.publish(game, GameEventType.CARD_DRAWN, Map.of(
                    "player", game.currentTurnPlayer(),
                    "handSize", game.hand().getOrDefault(game.currentTurnPlayer(), List.of()).size(),
                    "deckRemaining", game.deckCardsRemaining().getOrDefault(game.currentTurnPlayer(), 0)
                ));
            }

            case ATTACH_ENERGY -> {
                attachEnergyToActive(game, game.currentTurnPlayer());
                gameEngineFacade.publish(game, GameEventType.ENERGY_ATTACHED, Map.of(
                    "player", game.currentTurnPlayer(),
                    "attachedEnergy", game.activeAttachedEnergy().getOrDefault(game.currentTurnPlayer(), 0)
                ));
            }

            case PLAY_SUPPORTER -> {
                game.turnFlags().setSupporterPlayed(true);
                gameEngineFacade.publish(game, GameEventType.SUPPORTER_PLAYED, Map.of("player", game.currentTurnPlayer()));
            }

            case RETREAT -> {
                game.turnFlags().setRetreated(true);
                gameEngineFacade.publish(game, GameEventType.RETREAT_DECLARED, Map.of("player", game.currentTurnPlayer()));
            }

            case ATTACK -> {
                game.turnFlags().setAttacked(true);
                AttackContext context = gameEngineFacade.resolveAttack(game);

                UUID attacker = game.currentTurnPlayer();
                UUID defender = opponentOf(game, attacker);

                int damage = damageCalculator.calculate(
                    context.damage(),
                    false,
                    false,
                    game.statusByPlayer().getOrDefault(
                        attacker,
                        EnumSet.noneOf(StatusCondition.class)
                    )
                );

                applyDamage(game, defender, damage);
                applyDamage(game, attacker, context.selfDamage());

                int prizesBeforeKo = game.prizeCardsRemaining().getOrDefault(attacker, 6);
                victoryService.applyKnockoutAndPrizes(
                    game,
                    attacker,
                    defender
                );
                publishKoAndPrizeIfNeeded(game, attacker, defender, prizesBeforeKo);
            }

            case END_TURN -> {
                gameEngineFacade.advanceTurnPhase(game);

                if (game.turnPhase() == TurnPhase.DRAW) {
                    startNextTurn(game);
                }
            }

            case TAKE_PRIZE -> {
                UUID player = game.currentTurnPlayer();

                game.prizeCardsRemaining().put(
                    player,
                    game.prizeCardsRemaining().getOrDefault(player, 6) - 1
                );
                gameEngineFacade.publish(game, GameEventType.PRIZE_TAKEN, Map.of(
                    "player", player,
                    "prizeCardsRemaining", game.prizeCardsRemaining().getOrDefault(player, 0)
                ));
            }

            case APPLY_SPECIAL_CONDITION -> {
                statusEffectManager.applyCondition(
                    game,
                    opponentOf(game, game.currentTurnPlayer()),
                    conditionToApply
                );
                gameEngineFacade.publish(game, GameEventType.STATUS_APPLIED, Map.of(
                    "target", opponentOf(game, game.currentTurnPlayer()),
                    "condition", conditionToApply.name()
                ));
            }

            case RESOLVE_BETWEEN_TURNS -> {
                resolveBetweenTurnsForBothPlayers(game);
                gameEngineFacade.publish(game, GameEventType.BETWEEN_TURNS_RESOLVED, Map.of(
                    "playerOneHp", game.activeHp().getOrDefault(game.playerOne(), 0),
                    "playerTwoHp", game.activeHp().getOrDefault(game.playerTwo(), 0)
                ));
                victoryService.closeGameIfNeeded(game);
                if (game.gameState() != GameState.FINISHED) {
                    gameEngineFacade.advanceTurnPhase(game);
                    startNextTurn(game);
                }
            }
        }

        victoryService.closeGameIfNeeded(game);
        if (game.gameState() == GameState.FINISHED) {
            gameEngineFacade.publish(game, GameEventType.GAME_FINISHED, Map.of("winner", game.winner()));
        }

        gameStateRepository.appendActionLog(
            game.id(),
            actionType,
            actingPlayer,
            game.gameState().name() + "/" + game.turnPhase().name(),
            Instant.now()
        );

        return gameStateRepository.save(game);
    }

    public List<String> actionLog(UUID gameId) {
        return gameStateRepository.actionLog(gameId);
    }

    public GameAggregate get(UUID gameId) {
        return gameStateRepository.latestSnapshot(gameId)
            .orElseThrow(() ->
                new IllegalArgumentException("Game not found: " + gameId)
            );
    }

    private UUID opponentOf(GameAggregate game, UUID player) {
        if (player == null) {
            return game.playerTwo();
        }

        return player.equals(game.playerOne())
            ? game.playerTwo()
            : game.playerOne();
    }

    private void drawOptionalMulliganCards(GameAggregate game, UUID player, int cards) {
        for (int i = 0; i < cards; i++) {
            drawOne(game, player);
        }
    }

    private void drawForTurn(GameAggregate game, UUID player) {
        if (game.firstTurn() && player.equals(game.firstPlayer())) {
            return;
        }
        if (!drawOne(game, player)) {
            game.setWinner(opponentOf(game, player));
            game.setGameState(GameState.FINISHED);
        }
    }

    private boolean drawOne(GameAggregate game, UUID player) {
        var deck = game.deck().get(player);
        if (deck == null || deck.isEmpty()) {
            return false;
        }
        game.hand().get(player).add(deck.remove(0));
        game.deckCardsRemaining().put(player, deck.size());
        return true;
    }

    private void attachEnergyToActive(GameAggregate game, UUID player) {
        game.activeAttachedEnergy().put(
            player,
            game.activeAttachedEnergy().getOrDefault(player, 0) + 1
        );
        game.turnFlags().setEnergyAttached(true);
    }

    private void applyDamage(GameAggregate game, UUID player, int damage) {
        if (player == null || damage <= 0) {
            return;
        }
        int counters = damage / 10;
        int newCounters = game.activeDamageCounters().getOrDefault(player, 0) + counters;
        game.activeDamageCounters().put(player, newCounters);
        game.activeHp().put(player, Math.max(0, game.activeMaxHp(player) - (newCounters * 10)));
    }

    private void resolveBetweenTurnsForBothPlayers(GameAggregate game) {
        statusEffectManager.resolveBetweenTurns(game, game.playerOne());
        statusEffectManager.resolveBetweenTurns(game, game.playerTwo());
        victoryService.applyKnockoutAndPrizes(game, game.playerOne(), game.playerTwo());
        victoryService.applyKnockoutAndPrizes(game, game.playerTwo(), game.playerOne());
    }

    private void startNextTurn(GameAggregate game) {
        game.setCurrentTurnPlayer(
            game.currentTurnPlayer() != null
                && game.currentTurnPlayer().equals(game.playerOne())
                ? game.playerTwo()
                : game.playerOne()
        );
        game.turnFlags().reset();
        game.setFirstTurn(false);
        gameEngineFacade.publish(game, GameEventType.TURN_STARTED, Map.of(
            "currentTurnPlayer", game.currentTurnPlayer(),
            "phase", game.turnPhase().name()
        ));
    }

    private void publishKoAndPrizeIfNeeded(GameAggregate game, UUID attacker, UUID defender, int prizesBeforeKo) {
        if (defender == null) {
            return;
        }
        int prizesAfterKo = game.prizeCardsRemaining().getOrDefault(attacker, 6);
        if (prizesAfterKo >= prizesBeforeKo) {
            return;
        }
        gameEngineFacade.publish(game, GameEventType.KO, Map.of(
            "attacker", attacker,
            "defender", defender
        ));
        gameEngineFacade.publish(game, GameEventType.PRIZE_TAKEN, Map.of(
            "player", attacker,
            "prizeCardsRemaining", prizesAfterKo
        ));
    }
}
