package com.utn.pokemontcg.game.application.service.deck;

import com.utn.pokemontcg.game.infrastructure.persistence.card.CachedCardEntity;
import com.utn.pokemontcg.game.infrastructure.persistence.card.CachedCardJpaRepository;
import com.utn.pokemontcg.game.presentation.dto.deck.DeckCardInput;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CardCacheService {

    private final CachedCardJpaRepository cachedCardRepository;

    public CardCacheService(CachedCardJpaRepository cachedCardRepository) {
        this.cachedCardRepository = cachedCardRepository;
    }

    public void upsertFromDeckCards(List<DeckCardInput> cards) {
        cards.forEach(card -> cachedCardRepository.save(toEntity(card)));
    }

    private CachedCardEntity toEntity(DeckCardInput card) {
        CachedCardEntity entity = new CachedCardEntity();
        entity.setCardId(card.cardId());
        entity.setName(card.name());
        entity.setSetId(normalizeSetId(card));
        entity.setType(card.type());
        entity.setSubtype(card.subtype());
        entity.setBasicEnergy(Boolean.TRUE.equals(card.basicEnergy()));
        entity.setBasicPokemon(Boolean.TRUE.equals(card.basicPokemon()));
        entity.setAceSpec(Boolean.TRUE.equals(card.aceSpec()));
        entity.setHp(card.hp());
        entity.setAttackDamage(card.attackDamage());
        entity.setAttackRequiredEnergy(card.attackRequiredEnergy());
        entity.setCachedAt(Instant.now());
        return entity;
    }

    private String normalizeSetId(DeckCardInput card) {
        if (card.setId() != null && !card.setId().isBlank()) {
            return card.setId();
        }
        int separator = card.cardId().indexOf('-');
        return separator > 0 ? card.cardId().substring(0, separator) : "";
    }
}
