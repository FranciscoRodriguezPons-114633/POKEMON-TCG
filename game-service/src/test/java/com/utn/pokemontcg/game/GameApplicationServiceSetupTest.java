package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.application.service.SetupEngineService;
import com.utn.pokemontcg.game.application.service.TurnActionValidator;
import com.utn.pokemontcg.game.application.service.VictoryService;
import com.utn.pokemontcg.game.application.service.deck.DeckService;

import com.utn.pokemontcg.game.domain.engine.DamageCalculator;
import com.utn.pokemontcg.game.domain.engine.RuleValidator;
import com.utn.pokemontcg.game.domain.engine.StatusEffectManager;

import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import com.utn.pokemontcg.game.infrastructure.repository.InMemoryGameStateRepository;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckResponse;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameApplicationServiceSetupTest {

    @Test
    void shouldRunInitialSetupAndActivateGame() {
        GameApplicationService service =
                new GameApplicationService(
                        new InMemoryGameStateRepository(),
                        new GameEngineFacade(new GameEventPublisher()),
                        new SetupEngineService(),
                        new TurnActionValidator(),
                        new VictoryService(),
                        new RuleValidator(),
                        new DamageCalculator(),
                        new StatusEffectManager()
                );

        GameAggregate created =
                service.create(UUID.randomUUID());

        GameAggregate joined =
                service.join(created.id(), UUID.randomUUID());

        GameAggregate setup =
                service.runInitialSetup(
                        joined.id(),
                        60,
                        10,
                        60,
                        12
                );

        assertEquals(GameState.ACTIVE, setup.gameState());

        assertNotNull(setup.firstPlayer());

        assertEquals(
                2,
                setup.setupByPlayer().size()
        );
    }

    @Test
    void shouldCreateJoinAndSetupFromRealDeckIds() {
        DeckService deckService = mock(DeckService.class);
        GameApplicationService service =
                new GameApplicationService(
                        new InMemoryGameStateRepository(),
                        new GameEngineFacade(new GameEventPublisher()),
                        new SetupEngineService(),
                        new TurnActionValidator(),
                        new VictoryService(),
                        new RuleValidator(),
                        new DamageCalculator(),
                        new StatusEffectManager(),
                        deckService
                );

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p1Deck = UUID.randomUUID();
        UUID p2Deck = UUID.randomUUID();

        when(deckService.get(p1Deck)).thenReturn(deck(p1Deck, p1));
        when(deckService.get(p2Deck)).thenReturn(deck(p2Deck, p2));

        GameAggregate created = service.create(p1, p1Deck);
        service.join(created.id(), p2, p2Deck);

        GameAggregate setup =
                service.runInitialSetup(
                        created.id(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(GameState.ACTIVE, setup.gameState());
        assertEquals(p1Deck, setup.deckIdsByPlayer().get(p1));
        assertEquals(p2Deck, setup.deckIdsByPlayer().get(p2));
        assertEquals(setup.deck().get(p1).size(), setup.deckCardsRemaining().get(p1));
        assertEquals(setup.deck().get(p2).size(), setup.deckCardsRemaining().get(p2));
        assertTrue(setup.cardCatalog().get(setup.activePokemon().get(p1)).isBasicPokemon());
        assertEquals(40, setup.cardCatalog().get(setup.activePokemon().get(p1)).attackDamage());
    }

    @Test
    void shouldUseRealCardDamageForKnockoutPrizesAndVictory() {
        DeckService deckService = mock(DeckService.class);
        GameApplicationService service =
                new GameApplicationService(
                        new InMemoryGameStateRepository(),
                        new GameEngineFacade(new GameEventPublisher()),
                        new SetupEngineService(),
                        new TurnActionValidator(),
                        new VictoryService(),
                        new RuleValidator(),
                        new DamageCalculator(),
                        new StatusEffectManager(),
                        deckService
                );

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p1Deck = UUID.randomUUID();
        UUID p2Deck = UUID.randomUUID();

        when(deckService.get(p1Deck)).thenReturn(deckWithAttackDamage(p1Deck, p1, 130));
        when(deckService.get(p2Deck)).thenReturn(deckWithAttackDamage(p2Deck, p2, 30));

        GameAggregate created = service.create(p1, p1Deck);
        service.join(created.id(), p2, p2Deck);
        GameAggregate setup = service.runInitialSetup(created.id(), null, null, null, null, null, null);

        setup.setCurrentTurnPlayer(p1);
        setup.setFirstTurn(false);
        setup.setTurnPhase(TurnPhase.ATTACK);
        setup.activeAttachedEnergy().put(p1, 1);
        setup.bench().get(p2).clear();

        GameAggregate afterAttack = service.executeAction(created.id(), GameActionType.ATTACK);

        assertEquals(GameState.FINISHED, afterAttack.gameState());
        assertEquals(p1, afterAttack.winner());
        assertEquals(5, afterAttack.prizeCardsRemaining().get(p1));
        assertFalse(afterAttack.discardPile().get(p2).isEmpty());
    }

    private DeckResponse deck(UUID deckId, UUID playerId) {
        return new DeckResponse(
            deckId,
            playerId,
            "Mazo real XY1",
            List.of(
                new DeckCardInput(
                    "xy1-1",
                    "Basic Real",
                    "xy1",
                    4,
                    "Pokemon",
                    "Basic",
                    false,
                    true,
                    false,
                    130,
                    40,
                    1
                ),
                new DeckCardInput(
                    "xy1-2",
                    "Basic Energy",
                    "xy1",
                    56,
                    "Energy",
                    "Basic",
                    true,
                    false,
                    false,
                    null,
                    null,
                    null
                )
            ),
            new DeckValidationResult(true, List.of())
        );
    }

    private DeckResponse deckWithAttackDamage(UUID deckId, UUID playerId, int attackDamage) {
        return new DeckResponse(
            deckId,
            playerId,
            "Mazo real KO",
            List.of(
                new DeckCardInput(
                    "xy1-1",
                    "Basic Real",
                    "xy1",
                    4,
                    "Pokemon",
                    "Basic",
                    false,
                    true,
                    false,
                    120,
                    attackDamage,
                    1
                ),
                new DeckCardInput(
                    "xy1-2",
                    "Basic Energy",
                    "xy1",
                    56,
                    "Energy",
                    "Basic",
                    true,
                    false,
                    false,
                    null,
                    null,
                    null
                )
            ),
            new DeckValidationResult(true, List.of())
        );
    }
}
