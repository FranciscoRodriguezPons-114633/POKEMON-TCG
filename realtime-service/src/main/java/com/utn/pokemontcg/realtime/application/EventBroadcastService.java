package com.utn.pokemontcg.realtime.application;

import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public EventBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(GameEventMessage event) {
        messagingTemplate.convertAndSend("/topic/games/" + event.gameId() + "/events", event);
    }
}
