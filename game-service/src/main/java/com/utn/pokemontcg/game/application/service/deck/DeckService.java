package com.utn.pokemontcg.game.application.service.deck;

import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckCardEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.deck.DeckJpaRepository;
import com.utn.pokemontcg.game.presentation.dto.deck.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeckService {

    private final DeckJpaRepository deckRepository;
    private final DeckValidationService deckValidationService;
    private final CardCacheService cardCacheService;

    public DeckService(DeckJpaRepository deckRepository, DeckValidationService deckValidationService, CardCacheService cardCacheService) {
        this.deckRepository = deckRepository;
        this.deckValidationService = deckValidationService;
        this.cardCacheService = cardCacheService;
    }

    public DeckResponse create(DeckUpsertRequest request) {
        ensureValid(request.cards());
        DeckEntity entity = new DeckEntity();
        applyRequest(entity, request);
        cardCacheService.upsertFromDeckCards(request.cards());
        DeckEntity saved = deckRepository.save(entity);
        return toResponse(saved);
    }

    public DeckResponse update(UUID deckId, DeckUpsertRequest request) {
        ensureValid(request.cards());
        DeckEntity entity = deckRepository.findById(deckId)
            .orElseThrow(() -> new IllegalArgumentException("Deck no encontrado: " + deckId));
        applyRequest(entity, request);
        cardCacheService.upsertFromDeckCards(request.cards());
        DeckEntity saved = deckRepository.save(entity);
        return toResponse(saved);
    }

    public DeckResponse get(UUID deckId) {
        return deckRepository.findById(deckId)
            .map(this::toResponse)
            .orElseThrow(() -> new IllegalArgumentException("Deck no encontrado: " + deckId));
    }

    public void delete(UUID deckId) {
        deckRepository.deleteById(deckId);
    }

    @Transactional(readOnly = true)
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
            card.setSetId(setIdOf(input));
            card.setQuantity(input.quantity());
            card.setType(input.type());
            card.setSubtype(input.subtype());
            card.setBasicEnergy(input.basicEnergy());
            card.setBasicPokemon(input.basicPokemon());
            card.setAceSpec(input.aceSpec());
            card.setHp(input.hp());
            card.setAttackDamage(input.attackDamage());
            card.setAttackRequiredEnergy(input.attackRequiredEnergy());
            entity.getCards().add(card);
        }
    }

    private DeckResponse toResponse(DeckEntity entity) {
        List<DeckCardInput> cards = entity.getCards().stream()
            .map(card -> new DeckCardInput(card.getCardId(), card.getName(), card.getSetId(), card.getQuantity(), card.getType(),
                card.getSubtype(), card.isBasicEnergy(), card.isBasicPokemon(), card.isAceSpec(),
                card.getHp(), card.getAttackDamage(), card.getAttackRequiredEnergy()))
            .toList();
        return new DeckResponse(entity.getId(), entity.getPlayerId(), entity.getName(), cards, deckValidationService.validate(cards));
    }

    private void ensureValid(List<DeckCardInput> cards) {
        DeckValidationResult result = deckValidationService.validate(cards);
        if (!result.valid()) {
            throw new IllegalArgumentException(String.join(" ", result.errors()));
        }
    }

    private String setIdOf(DeckCardInput card) {
        if (card.setId() != null && !card.setId().isBlank()) {
            return card.setId();
        }
        int separator = card.cardId().indexOf('-');
        return separator > 0 ? card.cardId().substring(0, separator) : "";
    }
}
