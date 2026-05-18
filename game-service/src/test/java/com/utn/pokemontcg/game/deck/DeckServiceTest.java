package com.utn.pokemontcg.game.deck;

import com.utn.pokemontcg.game.application.service.deck.CardCacheService;
import com.utn.pokemontcg.game.application.service.deck.DeckService;
import com.utn.pokemontcg.game.application.service.deck.DeckValidationService;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckJpaRepository;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckUpsertRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeckServiceTest {

    @Test
    void createValidXy1DeckPersistsDeckAndCachesCards() {
        DeckJpaRepository repository = mock(DeckJpaRepository.class);
        CardCacheService cacheService = mock(CardCacheService.class);
        DeckService service = new DeckService(repository, new DeckValidationService(), cacheService);
        List<DeckCardInput> cards = List.of(
            new DeckCardInput("xy1-1", "Basic", "xy1", 4, "Pokemon", "Basic", false, true, false, 120, 30, 1),
            new DeckCardInput("xy1-2", "Energy", "xy1", 56, "Energy", "Basic", true, false, false, null, null, null)
        );
        DeckUpsertRequest request = new DeckUpsertRequest(UUID.randomUUID(), "XY1 Test", cards);
        when(repository.save(any(DeckEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request);

        assertTrue(response.validation().valid());
        verify(repository).save(any(DeckEntity.class));
        verify(cacheService).upsertFromDeckCards(cards);
    }

    @Test
    void createRejectsDeckWithCardsOutsideXy1() {
        DeckService service = new DeckService(
            mock(DeckJpaRepository.class),
            new DeckValidationService(),
            mock(CardCacheService.class)
        );
        List<DeckCardInput> cards = List.of(
            new DeckCardInput("bw1-1", "Wrong Set", "bw1", 4, "Pokemon", "Basic", false, true, false, 120, 30, 1),
            new DeckCardInput("xy1-2", "Energy", "xy1", 56, "Energy", "Basic", true, false, false, null, null, null)
        );

        assertThrows(IllegalArgumentException.class,
            () -> service.create(new DeckUpsertRequest(UUID.randomUUID(), "Invalid", cards)));
    }
}
