package com.utn.pokemontcg.application.realtime;

import com.utn.pokemontcg.application.service.GameService;
import com.utn.pokemontcg.domain.model.Game;
import com.utn.pokemontcg.presentation.dto.response.GameStateResponse;
import com.utn.pokemontcg.presentation.dto.websocket.GameActionMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameRealtimeService {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameRealtimeService(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    public GameStateResponse processAction(UUID gameId, GameActionMessage actionMessage) {
        Game game = switch (actionMessage.type()) {
            case END_MAIN -> gameService.endMainPhase(gameId);
            case ATTACK -> gameService.resolveAttack(gameId);
            case NEXT_TURN -> gameService.nextTurn(gameId);
        };

        GameStateResponse response = GameStateResponse.from(game);
        messagingTemplate.convertAndSend(topic(gameId), response);
        return response;
    }

    public void publishGameState(UUID gameId) {
        GameStateResponse response = GameStateResponse.from(gameService.getById(gameId));
        messagingTemplate.convertAndSend(topic(gameId), response);
    }

    private String topic(UUID gameId) {
        return "/topic/games/" + gameId + "/state";
    }
}
