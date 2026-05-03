package com.utn.pokemontcg.game;

import com.utn.pokemontcg.game.application.service.GameApplicationService;
import com.utn.pokemontcg.game.application.service.SetupEngineService;
import com.utn.pokemontcg.game.domain.event.GameEventPublisher;
import com.utn.pokemontcg.game.domain.facade.GameEngineFacade;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.infrastructure.repository.InMemoryGameStateRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameApplicationServiceSetupTest {

    @Test
    void shouldRunInitialSetupAndActivateGame() {
        GameApplicationService service = new GameApplicationService(
            new InMemoryGameStateRepository(),
            new GameEngineFacade(new GameEventPublisher()),
            new SetupEngineService()
        );

        GameAggregate created = service.create(UUID.randomUUID());
        GameAggregate joined = service.join(created.id(), UUID.randomUUID());

        GameAggregate setup = service.runInitialSetup(joined.id(), 60, 10, 60, 12);

        assertEquals(GameState.ACTIVE, setup.gameState());
        assertNotNull(setup.firstPlayer());
        assertEquals(2, setup.setupByPlayer().size());
    }
}
