package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.TurnActionValidator;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnActionValidatorTest {

    private final TurnActionValidator validator = new TurnActionValidator();

    @Test
    void shouldRejectAttachEnergyOutsideMainPhase() {
        UUID player = UUID.randomUUID();
        GameAggregate game = new GameAggregate(player);
        game.setGameState(GameState.ACTIVE);
        game.setCurrentTurnPlayer(player);
        game.setTurnPhase(TurnPhase.DRAW);

        assertThrows(IllegalStateException.class,
            () -> validator.validate(game, GameActionType.ATTACH_ENERGY));
    }

    @Test
    void shouldAllowAttachEnergyInMainWhenNotUsed() {
        UUID player = UUID.randomUUID();
        GameAggregate game = new GameAggregate(player);
        game.setGameState(GameState.ACTIVE);
        game.setCurrentTurnPlayer(player);
        game.setTurnPhase(TurnPhase.MAIN);

        assertDoesNotThrow(() -> validator.validate(game, GameActionType.ATTACH_ENERGY));
    }
}
