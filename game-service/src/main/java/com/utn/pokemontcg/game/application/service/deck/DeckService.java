package com.utn.pokemontcg.game.application.service.deck;

import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckCardEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckJpaRepository;
import com.utn.pokemontcg.game.presentation.dto.deck.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckJpaRepository deckRepository;
    private final DeckValidationService deckValidationService;

    public DeckService(DeckJpaRepository deckRepository, DeckValidationService deckValidationService) {
        this.deckRepository = deckRepository;
        this.deckValidationService = deckValidationService;
    }

    public DeckResponse create(DeckUpsertRequest request) {
        DeckEntity entity = new DeckEntity();
        applyRequest(entity, request);
        DeckEntity saved = deckRepository.save(entity);
        return toResponse(saved);
    }

    public DeckResponse update(UUID deckId, DeckUpsertRequest request) {
        DeckEntity entity = deckRepository.findById(deckId)
            .orElseThrow(() -> new IllegalArgumentException("Deck no encontrado: " + deckId));
        applyRequest(entity, request);
        DeckEntity saved = deckRepository.save(entity);
        return toResponse(saved);
    }

    public void delete(UUID deckId) {
        deckRepository.deleteById(deckId);
    }

    public List<DeckResponse> listByPlayer(UUID playerId) {
        return deckRepository.findByPlayerId(playerId).stream().map(this::toResponse).toList();
    }

    private void applyRequest(DeckEntity entity, DeckUpsertRequest request) {
        entity.setPlayerId(request.playerId());
        entity.setName(request.name());
        entity.getCards().clear();
        for (DeckCardInput input : request.cards()) {
            DeckCardEntity card = new DeckCardEntity();
            card.setDeck(entity);
            card.setCardId(input.cardId());
            card.setName(input.name());
            card.setQuantity(input.quantity());
            card.setType(input.type());
            card.setSubtype(input.subtype());
            card.setBasicEnergy(input.basicEnergy());
            card.setBasicPokemon(input.basicPokemon());
            card.setAceSpec(input.aceSpec());
            entity.getCards().add(card);
        }
    }

    private DeckResponse toResponse(DeckEntity entity) {
        List<DeckCardInput> cards = entity.getCards().stream()
            .map(card -> new DeckCardInput(card.getCardId(), card.getName(), card.getQuantity(), card.getType(),
                card.getSubtype(), card.isBasicEnergy(), card.isBasicPokemon(), card.isAceSpec()))
            .toList();
        return new DeckResponse(entity.getId(), entity.getPlayerId(), entity.getName(), cards, deckValidationService.validate(cards));
    }
}
