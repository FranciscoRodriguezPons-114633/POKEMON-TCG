package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.GameActionType;
import com.utn.pokemontcg.game.domain.model.GameAggregate;
import com.utn.pokemontcg.game.domain.model.GameState;
import com.utn.pokemontcg.game.domain.model.StatusCondition;
import com.utn.pokemontcg.game.domain.model.TurnPhase;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class TurnActionValidator {

    public void validate(GameAggregate game, GameActionType action) {
        TurnPhase phase = game.turnPhase();
        require(game.gameState() == GameState.ACTIVE, "La partida debe estar ACTIVE para ejecutar acciones");
        require(game.currentTurnPlayer() != null, "No hay jugador activo para el turno");

        switch (action) {
            case DRAW -> require(phase == TurnPhase.DRAW, "DRAW solo en fase DRAW");
            case ATTACH_ENERGY -> {
                require(phase == TurnPhase.MAIN, "ATTACH_ENERGY solo en MAIN");
                require(!game.turnFlags().energyAttached(), "Solo se puede unir 1 energía por turno");
            }
            case PLAY_SUPPORTER -> {
                require(phase == TurnPhase.MAIN, "PLAY_SUPPORTER solo en MAIN");
                require(!game.turnFlags().supporterPlayed(), "Solo se puede jugar 1 partidario por turno");
            }
            case RETREAT -> {
                require(phase == TurnPhase.MAIN, "RETREAT solo en MAIN");
                require(!game.turnFlags().retreated(), "Solo se puede retirar 1 vez por turno");
                require(!activeHas(game, StatusCondition.ASLEEP), "Un Pokemon Dormido no puede retirarse");
                require(!activeHas(game, StatusCondition.PARALYZED), "Un Pokemon Paralizado no puede retirarse");
            }
            case ATTACK -> {
                require(phase == TurnPhase.ATTACK, "ATTACK solo en fase ATTACK");
                require(!game.firstTurn(), "No se puede atacar en el primer turno global");
                require(game.activePokemon().get(game.currentTurnPlayer()) != null, "El jugador activo no tiene Pokemon Activo");
                require(!activeHas(game, StatusCondition.ASLEEP), "Un Pokemon Dormido no puede atacar");
                require(!activeHas(game, StatusCondition.PARALYZED), "Un Pokemon Paralizado no puede atacar");
            }
            case END_TURN -> require(
                phase == TurnPhase.MAIN || phase == TurnPhase.ATTACK || phase == TurnPhase.BETWEEN_TURNS,
                "END_TURN solo en MAIN, ATTACK o BETWEEN_TURNS"
            );
            case TAKE_PRIZE -> require(phase == TurnPhase.BETWEEN_TURNS, "TAKE_PRIZE solo en BETWEEN_TURNS");
            case APPLY_SPECIAL_CONDITION -> require(phase == TurnPhase.ATTACK || phase == TurnPhase.BETWEEN_TURNS,
                "APPLY_SPECIAL_CONDITION solo en ATTACK o BETWEEN_TURNS");
            case RESOLVE_BETWEEN_TURNS -> require(phase == TurnPhase.BETWEEN_TURNS,
                "RESOLVE_BETWEEN_TURNS solo en BETWEEN_TURNS");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private boolean activeHas(GameAggregate game, StatusCondition condition) {
        EnumSet<StatusCondition> statuses = game.statusByPlayer()
            .getOrDefault(game.currentTurnPlayer(), EnumSet.noneOf(StatusCondition.class));
        return statuses.contains(condition);
    }
}
