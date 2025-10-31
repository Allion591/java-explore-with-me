package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatService statService;

    @Test
    void createHit_shouldCreateAndReturnHit() throws Exception {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.168.1.1");
        endpointHit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        when(statService.saveHit(any(EndpointHit.class))).thenReturn(endpointHit);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointHit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("ewm-main-service"))
                .andExpect(jsonPath("$.uri").value("/events/1"))
                .andExpect(jsonPath("$.ip").value("192.168.1.1"));
    }

    @Test
    void getStats_shouldReturnStatistics() throws Exception {
        List<ViewStats> stats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 15L),
                new ViewStats("ewm-main-service", "/events/2", 10L)
        );

        when(statService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", "2024-01-01 00:00:00")
                        .param("end", "2024-01-02 00:00:00")
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(15));
    }

    @Test
    void getStats_shouldValidateParameters() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isBadRequest());
    }
}