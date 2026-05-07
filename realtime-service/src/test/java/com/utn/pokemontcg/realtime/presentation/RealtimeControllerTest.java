package com.utn.pokemontcg.realtime.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.pokemontcg.realtime.application.EventBroadcastService;
import com.utn.pokemontcg.realtime.dto.GameEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

        mockMvc.perform(post("/internal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventBroadcastService).broadcast(any(GameEventMessage.class));
    }
}
