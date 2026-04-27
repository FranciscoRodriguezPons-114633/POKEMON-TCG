package com.utn.pokemontcg.presentation.websocket;

import com.utn.pokemontcg.application.realtime.GameRealtimeService;
import com.utn.pokemontcg.presentation.dto.websocket.GameActionMessage;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class GameWebSocketController {

    private final GameRealtimeService gameRealtimeService;

    public GameWebSocketController(GameRealtimeService gameRealtimeService) {
        this.gameRealtimeService = gameRealtimeService;
    }

    @MessageMapping("/games/{gameId}/action")
    public void onGameAction(@DestinationVariable UUID gameId,
                             @Valid @Payload GameActionMessage actionMessage) {
        gameRealtimeService.processAction(gameId, actionMessage);
    }
}
