package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StatService statService;

    @InjectMocks
    private StatsController statsController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(statsController).build();
    }

    @Test
    void createHit_shouldReturnCreatedStatusAndEndpointHit() throws Exception {
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
        verify(statService).saveHit(any(EndpointHit.class));
    }

    @Test
    void createHit_shouldReturnBadRequestWhenInvalidEndpointHit() throws Exception {
        EndpointHit invalidHit = new EndpointHit();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_shouldReturnStatsList() throws Exception {
        List<ViewStats> expectedStats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 15L),
                new ViewStats("ewm-main-service", "/events/2", 10L)
        );

        when(statService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), any()))
                .thenReturn(expectedStats);

        mockMvc.perform(get("/stats")
                        .param("start", "2024-01-01 00:00:00")
                        .param("end", "2024-01-02 00:00:00")
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(15))
                .andExpect(jsonPath("$[1].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[1].uri").value("/events/2"))
                .andExpect(jsonPath("$[1].hits").value(10));

        verify(statService).getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), eq(true));
    }

    @Test
    void getStats_shouldReturnBadRequestWhenMissingRequiredParams() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_shouldHandleEmptyUris() throws Exception {
        when(statService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2024-01-01 00:00:00")
                        .param("end", "2024-01-02 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(statService).getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), isNull());
    }

    @Test
    void getStats_shouldHandleNullUniqueParameter() throws Exception {
        when(statService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2024-01-01 00:00:00")
                        .param("end", "2024-01-02 00:00:00")
                        .param("uris", "/events/1"))
                .andExpect(status().isOk());

        verify(statService).getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), isNull());
    }
}