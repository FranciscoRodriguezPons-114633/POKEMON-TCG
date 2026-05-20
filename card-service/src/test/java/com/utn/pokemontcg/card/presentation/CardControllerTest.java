package com.utn.pokemontcg.card.presentation;

import com.utn.pokemontcg.card.application.CardApiService;
import com.utn.pokemontcg.card.application.dto.CardSearchResponse;
import com.utn.pokemontcg.card.application.dto.CardSummaryResponse;
import com.utn.pokemontcg.card.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@ContextConfiguration(classes = {CardController.class, CorsConfig.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardApiService cardApiService;

    @Test
    void shouldSearchCards() throws Exception {
        when(cardApiService.search("set.id:xy1", 20)).thenReturn(new CardSearchResponse(
            "set.id:xy1",
            20,
            1,
            1,
            List.of(new CardSummaryResponse("xy1-1", "Venusaur-EX", "xy1", "Pokemon", List.of("Basic", "EX"), List.of("Grass"), 180, List.of(), List.of(), List.of()))
        ));

        mockMvc.perform(get("/api/cards").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.cards[0].id").value("xy1-1"))
            .andExpect(jsonPath("$.cards[0].hp").value(180));

        verify(cardApiService).search("set.id:xy1", 20);
    }

    @Test
    void shouldGetCardById() throws Exception {
        when(cardApiService.byId("xy1-1")).thenReturn(new CardSummaryResponse(
            "xy1-1",
            "Venusaur-EX",
            "xy1",
            "Pokemon",
            List.of("Basic", "EX"),
            List.of("Grass"),
            180,
            List.of(),
            List.of(),
            List.of()
        ));

        mockMvc.perform(get("/api/cards/xy1-1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("xy1-1"))
            .andExpect(jsonPath("$.name").value("Venusaur-EX"))
            .andExpect(jsonPath("$.setId").value("xy1"));

        verify(cardApiService).byId("xy1-1");
    }

    @Test
    void shouldAllowAnyOrigin() throws Exception {
        when(cardApiService.search("set.id:xy1", 20)).thenReturn(new CardSearchResponse(
            "set.id:xy1",
            20,
            0,
            0,
            List.of()
        ));

        mockMvc.perform(get("/api/cards")
                .header("Origin", "http://example.test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String origin = result.getResponse().getHeader("Access-Control-Allow-Origin");
                org.junit.jupiter.api.Assertions.assertEquals("http://example.test", origin);
            });
    }
}
