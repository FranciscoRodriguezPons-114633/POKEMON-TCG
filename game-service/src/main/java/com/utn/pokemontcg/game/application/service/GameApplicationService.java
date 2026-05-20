package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.application.repository.GameStateRepository;
import com.utn.pokemontcg.game.application.service.deck.DeckService;
import com.utn.pokemontcg.game.domain.chain.AttackContext;
import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;
import com.utn.pokemontcg.game.domain.event.GameEventType;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameCard;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final DeckService deckService;

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
        this(
            gameStateRepository,
            gameEngineFacade,
            setupEngineService,
            turnActionValidator,
            victoryService,
            ruleValidator,
            damageCalculator,
            statusEffectManager,
            null
        );
    }

    @Autowired
    public GameApplicationService(
        GameStateRepository gameStateRepository,
        GameEngineFacade gameEngineFacade,
        SetupEngineService setupEngineService,
        TurnActionValidator turnActionValidator,
        VictoryService victoryService,
        RuleValidator ruleValidator,
        DamageCalculator damageCalculator,
        StatusEffectManager statusEffectManager,
        DeckService deckService
    ) {
        this.gameStateRepository = gameStateRepository;
        this.gameEngineFacade = gameEngineFacade;
        this.setupEngineService = setupEngineService;
        this.turnActionValidator = turnActionValidator;
        this.victoryService = victoryService;
        this.ruleValidator = ruleValidator;
        this.damageCalculator = damageCalculator;
        this.statusEffectManager = statusEffectManager;
        this.deckService = deckService;
    }

    public GameAggregate create(UUID playerId) {
        return create(playerId, null);
    }

    public GameAggregate create(UUID playerId, UUID deckId) {
        GameAggregate game = new GameAggregate(playerId);
        if (deckId != null) {
            game.deckIdsByPlayer().put(playerId, deckId);
        }
        GameAggregate saved = gameStateRepository.save(game);
        gameEngineFacade.publish(saved, GameEventType.GAME_CREATED, eventPayloadWithDeck("playerOne", playerId, deckId));
        return saved;
    }

    public GameAggregate join(UUID gameId, UUID playerId) {
        return join(gameId, playerId, null);
    }

    public GameAggregate join(UUID gameId, UUID playerId, UUID deckId) {
        GameAggregate game = get(gameId);
        game.setPlayerTwo(playerId);
        if (deckId != null) {
            game.deckIdsByPlayer().put(playerId, deckId);
        }
        GameAggregate saved = gameStateRepository.save(game);
        gameEngineFacade.publish(saved, GameEventType.PLAYER_JOINED, eventPayloadWithDeck("playerTwo", playerId, deckId));
        return saved;
    }

    public GameAggregate runInitialSetup(
        UUID gameId,
        int playerOneDeckSize,
        int playerOneBasicCount,
        int playerTwoDeckSize,
        int playerTwoBasicCount
    ) {
        return runInitialSetup(
            gameId,
            playerOneDeckSize,
            playerOneBasicCount,
            playerTwoDeckSize,
            playerTwoBasicCount,
            null,
            null
        );
    }

    public GameAggregate runInitialSetup(
        UUID gameId,
        Integer playerOneDeckSize,
        Integer playerOneBasicCount,
        Integer playerTwoDeckSize,
        Integer playerTwoBasicCount,
        UUID playerOneDeckId,
        UUID playerTwoDeckId
    ) {
        GameAggregate game = get(gameId);

        if (game.playerTwo() == null) {
            throw new IllegalStateException("Two players are required before setup");
        }

        registerRequestedDeckIds(game, playerOneDeckId, playerTwoDeckId);

        UUID resolvedPlayerOneDeckId = game.deckIdsByPlayer().get(game.playerOne());
        UUID resolvedPlayerTwoDeckId = game.deckIdsByPlayer().get(game.playerTwo());

        var p1 = hasBothDecks(resolvedPlayerOneDeckId, resolvedPlayerTwoDeckId)
            ? setupEngineService.preparePlayerBoard(
                game.playerOne(),
                realDeckCardsFor(game.playerOne(), resolvedPlayerOneDeckId),
                0,
                game
            )
            : setupEngineService.preparePlayerBoard(
                game.playerOne(),
                require(playerOneDeckSize, "playerOneDeckSize"),
                require(playerOneBasicCount, "playerOneBasicCount"),
                0,
                game
            );

        var p2 = hasBothDecks(resolvedPlayerOneDeckId, resolvedPlayerTwoDeckId)
            ? setupEngineService.preparePlayerBoard(
                game.playerTwo(),
                realDeckCardsFor(game.playerTwo(), resolvedPlayerTwoDeckId),
                p1.mulligans(),
                game
            )
            : setupEngineService.preparePlayerBoard(
                game.playerTwo(),
                require(playerTwoDeckSize, "playerTwoDeckSize"),
                require(playerTwoBasicCount, "playerTwoBasicCount"),
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

                VictoryService.KnockoutResult knockout = victoryService.applyKnockoutAndPrizes(
                    game,
                    attacker,
                    defender
                );
                publishKnockoutResult(game, knockout);
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
                closeGameAndPublishIfNeeded(game);
                if (game.gameState() != GameState.FINISHED) {
                    gameEngineFacade.advanceTurnPhase(game);
                    startNextTurn(game);
                }
            }
        }

        closeGameAndPublishIfNeeded(game);

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
        publishKnockoutResult(game, victoryService.applyKnockoutAndPrizes(game, game.playerOne(), game.playerTwo()));
        publishKnockoutResult(game, victoryService.applyKnockoutAndPrizes(game, game.playerTwo(), game.playerOne()));
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

    private void publishKnockoutResult(GameAggregate game, VictoryService.KnockoutResult knockout) {
        if (knockout == null || !knockout.knockedOut()) {
            return;
        }
        Map<String, Object> koPayload = new HashMap<>();
        koPayload.put("attacker", knockout.attacker());
        koPayload.put("defender", knockout.defender());
        koPayload.put("knockedOutCard", knockout.knockedOutCard());
        koPayload.put("promotedCard", knockout.promotedCard());
        koPayload.put("defenderHasNoPokemon", knockout.defenderHasNoPokemon());
        gameEngineFacade.publish(game, GameEventType.KO, koPayload);

        Map<String, Object> prizePayload = new HashMap<>();
        prizePayload.put("player", knockout.attacker());
        prizePayload.put("prizesTaken", knockout.prizesTaken());
        prizePayload.put("prizeCardsRemaining", knockout.prizeCardsRemaining());
        gameEngineFacade.publish(game, GameEventType.PRIZE_TAKEN, prizePayload);
    }

    private void closeGameAndPublishIfNeeded(GameAggregate game) {
        GameState previousState = game.gameState();
        UUID previousWinner = game.winner();
        victoryService.closeGameIfNeeded(game);
        if (previousState != GameState.FINISHED && game.gameState() == GameState.FINISHED) {
            gameEngineFacade.publish(game, GameEventType.GAME_FINISHED, Map.of(
                "winner", game.winner(),
                "reason", finishReason(game, previousWinner)
            ));
        }
    }

    private String finishReason(GameAggregate game, UUID previousWinner) {
        if (game.winner() == null || game.winner().equals(previousWinner)) {
            return "UNKNOWN";
        }
        UUID loser = game.winner().equals(game.playerOne()) ? game.playerTwo() : game.playerOne();
        if (game.prizeCardsRemaining().getOrDefault(game.winner(), 1) <= 0) {
            return "PRIZES";
        }
        if (!game.hasPokemonInPlay(loser)) {
            return "KO_TOTAL";
        }
        if (game.deck().getOrDefault(loser, List.of()).isEmpty()) {
            return "DECK_OUT";
        }
        return "UNKNOWN";
    }

    private Map<String, Object> eventPayloadWithDeck(String playerKey, UUID playerId, UUID deckId) {
        return deckId == null
            ? Map.of(playerKey, playerId)
            : Map.of(playerKey, playerId, "deckId", deckId);
    }

    private void registerRequestedDeckIds(GameAggregate game, UUID playerOneDeckId, UUID playerTwoDeckId) {
        if (playerOneDeckId != null) {
            game.deckIdsByPlayer().put(game.playerOne(), playerOneDeckId);
        }
        if (playerTwoDeckId != null) {
            game.deckIdsByPlayer().put(game.playerTwo(), playerTwoDeckId);
        }
    }

    private boolean hasBothDecks(UUID playerOneDeckId, UUID playerTwoDeckId) {
        return playerOneDeckId != null && playerTwoDeckId != null;
    }

    private int require(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(
                "Para setup manual se requiere " + fieldName + ". Para setup real enviá deckId de ambos jugadores."
            );
        }
        return value;
    }

    private List<GameCard> realDeckCardsFor(UUID playerId, UUID deckId) {
        if (deckService == null) {
            throw new IllegalStateException("DeckService is required for setup with real decks");
        }
        DeckResponse deck = deckService.get(deckId);
        if (!playerId.equals(deck.playerId())) {
            throw new IllegalArgumentException("El mazo " + deckId + " no pertenece al jugador " + playerId);
        }
        if (!deck.validation().valid()) {
            throw new IllegalArgumentException(String.join(" ", deck.validation().errors()));
        }

        List<GameCard> cards = new ArrayList<>();
        int copyIndex = 0;
        for (DeckCardInput input : deck.cards()) {
            for (int i = 0; i < input.quantity(); i++) {
                cards.add(toGameCard(playerId, input, copyIndex++));
            }
        }
        return cards;
    }

    private GameCard toGameCard(UUID playerId, DeckCardInput input, int copyIndex) {
        return new GameCard(
            input.cardId() + ":" + playerId + ":" + copyIndex,
            input.name(),
            input.type(),
            subtypesOf(input),
            input.hp() == null ? 0 : input.hp(),
            input.attackDamage() == null ? 0 : input.attackDamage(),
            input.attackRequiredEnergy() == null ? 0 : input.attackRequiredEnergy()
        );
    }

    private Set<String> subtypesOf(DeckCardInput input) {
        Set<String> subtypes = new LinkedHashSet<>();
        if (input.subtype() != null && !input.subtype().isBlank()) {
            for (String subtype : input.subtype().split(",")) {
                String trimmed = subtype.trim();
                if (!trimmed.isEmpty()) {
                    subtypes.add(trimmed);
                }
            }
        }
        if (input.basicPokemon() || input.basicEnergy()) {
            subtypes.add("Basic");
        }
        if (input.aceSpec()) {
            subtypes.add("ACE SPEC");
        }
        return subtypes;
    }
}
