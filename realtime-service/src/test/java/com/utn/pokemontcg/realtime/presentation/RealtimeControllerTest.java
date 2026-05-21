package com.utn.pokemontcg.realtime.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.realtime.application.EventBroadcastService;
import com.utn.pokemontcg.realtime.dto.GameEventEnvelope;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import com.utn.pokemontcg.realtime.dto.GameEventType;
import com.utn.pokemontcg.realtime.dto.PendingEventsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RealtimeController.class)
class RealtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventBroadcastService eventBroadcastService;

    @Test
    void shouldPublishEventFromGameService() throws Exception {
        GameEventMessage message = new GameEventMessage(
            UUID.randomUUID(),
            "TURN_STARTED",
            Map.of("turn", 1),
            Instant.parse("2026-05-06T18:00:00Z")
        );
        GameEventEnvelope envelope = new GameEventEnvelope(
            1L,
            1,
            message.gameId(),
            GameEventType.TURN_STARTED,
            message.payload(),
            message.occurredAt(),
            Instant.parse("2026-05-06T18:00:01Z")
        );
        when(eventBroadcastService.broadcast(any(GameEventMessage.class))).thenReturn(envelope);

        mockMvc.perform(post("/internal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sequence").value(1))
            .andExpect(jsonPath("$.schemaVersion").value(1))
            .andExpect(jsonPath("$.type").value("TURN_STARTED"));

        verify(eventBroadcastService).broadcast(any(GameEventMessage.class));
    }

    @Test
    void shouldReturnPendingEventsForReconnect() throws Exception {
        UUID gameId = UUID.randomUUID();
        GameEventMessage message = new GameEventMessage(
            gameId,
            "ATTACK_RESOLVED",
            Map.of("damage", 30),
            Instant.parse("2026-05-06T18:01:00Z")
        );
        GameEventEnvelope envelope = new GameEventEnvelope(
            2L,
            1,
            gameId,
            GameEventType.ATTACK_RESOLVED,
            message.payload(),
            message.occurredAt(),
            Instant.parse("2026-05-06T18:01:01Z")
        );
        when(eventBroadcastService.pendingEvents(gameId, 1L, Instant.parse("2026-05-06T18:00:00Z")))
            .thenReturn(new PendingEventsResponse(gameId, 2L, false, Instant.parse("2026-05-06T18:01:02Z"), List.of(envelope)));

        mockMvc.perform(get("/internal/events/{gameId}", gameId)
                .param("sinceSequence", "1")
                .param("since", "2026-05-06T18:00:00Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastSequence").value(2))
            .andExpect(jsonPath("$.replayFromMemory").value(false))
            .andExpect(jsonPath("$.events[0].sequence").value(2))
            .andExpect(jsonPath("$.events[0].type").value("ATTACK_RESOLVED"));
    }
}
