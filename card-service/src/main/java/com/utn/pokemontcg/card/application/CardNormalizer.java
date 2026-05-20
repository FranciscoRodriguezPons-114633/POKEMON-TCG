package com.utn.pokemontcg.card.application;

import com.utn.pokemontcg.card.application.dto.CardAttackResponse;
import com.utn.pokemontcg.card.application.dto.CardSearchResponse;
import com.utn.pokemontcg.card.application.dto.CardSummaryResponse;
import com.utn.pokemontcg.card.application.dto.TypeModifierResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CardNormalizer {

    public CardSearchResponse searchResponse(String query, int pageSize, Map<String, Object> raw) {
        List<CardSummaryResponse> cards = maps(raw.get("data")).stream()
            .map(this::card)
            .toList();
        return new CardSearchResponse(
            query,
            pageSize,
            number(raw.get("count")),
            number(raw.get("totalCount")),
            cards
        );
    }

    public CardSummaryResponse byIdResponse(Map<String, Object> raw) {
        return card(map(raw.get("data")));
    }

    private CardSummaryResponse card(Map<String, Object> raw) {
        Map<String, Object> set = map(raw.get("set"));
        return new CardSummaryResponse(
            text(raw.get("id")),
            text(raw.get("name")),
            text(set.get("id")),
            text(raw.get("supertype")),
            strings(raw.get("subtypes")),
            strings(raw.get("types")),
            nullableNumber(raw.get("hp")),
            maps(raw.get("attacks")).stream().map(this::attack).toList(),
            maps(raw.get("weaknesses")).stream().map(this::modifier).toList(),
            maps(raw.get("resistances")).stream().map(this::modifier).toList()
        );
    }

    private CardAttackResponse attack(Map<String, Object> raw) {
        return new CardAttackResponse(
            text(raw.get("name")),
            text(raw.get("text")),
            text(raw.get("damage")),
            strings(raw.get("cost")),
            number(raw.get("convertedEnergyCost"))
        );
    }

    private TypeModifierResponse modifier(Map<String, Object> raw) {
        return new TypeModifierResponse(text(raw.get("type")), text(raw.get("value")));
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int number(Object value) {
        Integer number = nullableNumber(value);
        return number == null ? 0 : number;
    }

    private Integer nullableNumber(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> maps(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
        }
        return List.of();
    }

    private List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
