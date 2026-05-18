package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.domain.chain.AttackContext;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
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
        gameStateRepository.save(game);
        return game;
    }

    public GameAggregate join(UUID gameId, UUID playerId) {
        GameAggregate game = get(gameId);
        game.setPlayerTwo(playerId);
        return gameStateRepository.save(game);
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

        return saved;
    }

    public GameAggregate executeAction(UUID gameId, GameActionType actionType) {
        GameAggregate game = get(gameId);

        turnActionValidator.validate(game, actionType);
        ruleValidator.validate(game, actionType);

        switch (actionType) {
            case DRAW -> {
                drawForTurn(game, game.currentTurnPlayer());
                gameEngineFacade.advanceTurnPhase(game);
            }

            case ATTACH_ENERGY ->
                attachEnergyToActive(game, game.currentTurnPlayer());

            case PLAY_SUPPORTER ->
                game.turnFlags().setSupporterPlayed(true);

            case RETREAT ->
                game.turnFlags().setRetreated(true);

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

                victoryService.applyKnockoutAndPrizes(
                    game,
                    attacker,
                    defender
                );
            }

            case END_TURN -> {
                gameEngineFacade.advanceTurnPhase(game);

                if (game.turnPhase() == TurnPhase.DRAW) {
                    game.setCurrentTurnPlayer(
                        game.currentTurnPlayer() != null
                            && game.currentTurnPlayer().equals(game.playerOne())
                            ? game.playerTwo()
                            : game.playerOne()
                    );

                    game.turnFlags().reset();
                    game.setFirstTurn(false);
                }
            }

            case TAKE_PRIZE -> {
                UUID player = game.currentTurnPlayer();

                game.prizeCardsRemaining().put(
                    player,
                    game.prizeCardsRemaining().getOrDefault(player, 6) - 1
                );
            }

            case APPLY_SPECIAL_CONDITION ->
                statusEffectManager.applyCondition(
                    game,
                    opponentOf(game, game.currentTurnPlayer()),
                    StatusCondition.POISONED
                );

            case RESOLVE_BETWEEN_TURNS ->
                resolveBetweenTurnsForBothPlayers(game);
        }

        victoryService.closeGameIfNeeded(game);

        gameStateRepository.appendActionLog(
            game.id(),
            actionType,
            game.currentTurnPlayer(),
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
        game.activeHp().put(player, Math.max(0, 120 - (newCounters * 10)));
    }

    private void resolveBetweenTurnsForBothPlayers(GameAggregate game) {
        statusEffectManager.resolveBetweenTurns(game, game.playerOne());
        statusEffectManager.resolveBetweenTurns(game, game.playerTwo());
        victoryService.applyKnockoutAndPrizes(game, game.playerOne(), game.playerTwo());
        victoryService.applyKnockoutAndPrizes(game, game.playerTwo(), game.playerOne());
    }
}
