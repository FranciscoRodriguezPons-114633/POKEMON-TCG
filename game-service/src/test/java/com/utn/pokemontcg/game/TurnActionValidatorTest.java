package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.TurnActionValidator;
import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnActionValidatorTest {

    private final TurnActionValidator validator = new TurnActionValidator();

    @Test
    void shouldRejectAttachEnergyOutsideMainPhase() {
        GameAggregate game = new GameAggregate(UUID.randomUUID());
        game.setTurnPhase(TurnPhase.DRAW);

        assertThrows(IllegalStateException.class,
            () -> validator.validate(game, GameActionType.ATTACH_ENERGY));
    }

    @Test
    void shouldAllowAttachEnergyInMainWhenNotUsed() {
        GameAggregate game = new GameAggregate(UUID.randomUUID());
        game.setTurnPhase(TurnPhase.MAIN);

        assertDoesNotThrow(() -> validator.validate(game, GameActionType.ATTACH_ENERGY));
    }
}
