package com.utn.pokemontcg.card.presentation;

import com.utn.pokemontcg.card.application.CardApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardApiService cardApiService;

    @Test
    void shouldSearchCards() throws Exception {
        when(cardApiService.search("set.id:xy1", 20)).thenReturn(Map.of("count", 1));

        mockMvc.perform(get("/api/cards").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1));

        verify(cardApiService).search("set.id:xy1", 20);
    }

    @Test
    void shouldGetCardById() throws Exception {
        when(cardApiService.byId("xy1-1")).thenReturn(Map.of("id", "xy1-1"));

        mockMvc.perform(get("/api/cards/xy1-1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("xy1-1"));

        verify(cardApiService).byId("xy1-1");
    }
}
