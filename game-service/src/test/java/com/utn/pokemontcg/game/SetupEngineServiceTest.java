package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.SetupEngineService;
import com.utn.pokemontcg.game.domain.model.PlayerSetupState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetupEngineServiceTest {

    @Test
    void shouldPrepareValidInitialSetupState() {
        SetupEngineService service = new SetupEngineService();

        PlayerSetupState setup = service.preparePlayer(60, 10, 2);

        assertTrue(setup.hasActive());
        assertEquals(6, setup.prizeCount());
        assertTrue(setup.benchCount() >= 0 && setup.benchCount() <= 5);
        assertEquals(9, setup.handSize());
        assertTrue(setup.mulligans() >= 0);
    }
}
