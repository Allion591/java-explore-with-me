package ru.practicum.main.controller.publics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.stat.ConnectionToStatistics;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ConnectionToStatistics statistics;

    private final String validAnnotation = "This is a valid annotation that meets the minimum length requirement " +
            "of 20 characters";
    private final String validDescription = "This is a valid description that meets the minimum length requirement " +
            "of 20 characters and is long enough to pass validation";
    private final String validTitle = "Valid Event Title That Meets Requirements";

    @Test
    void getEvents_ShouldReturn200AndEventList() throws Exception {
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title(validTitle)
                .annotation(validAnnotation)
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .build();

        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class)))
                .thenReturn(List.of(eventShortDto));

        doNothing().when(statistics).postHit(any());

        mockMvc.perform(get("/events")
                        .param("text", "concert")
                        .param("onlyAvailable", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(validTitle));

        verify(eventService, times(1)).getEventsPublic(any(EventPublicFilterRequest.class));
        verify(statistics, times(1)).postHit(any());
    }

    @Test
    void getEvents_WithDefaultParameters_ShouldReturn200() throws Exception {
        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class)))
                .thenReturn(List.of());

        doNothing().when(statistics).postHit(any());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEventsPublic(any(EventPublicFilterRequest.class));
        verify(statistics, times(1)).postHit(any());
    }

    @Test
    void getEvents_WithInvalidDateRange_ShouldReturn400() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/events")
                        .param("rangeStart", start.toString())
                        .param("rangeEnd", end.toString()))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventsPublic(any());
        verify(statistics, never()).postHit(any());
    }

    @Test
    void getEvents_WithInvalidPagination_ShouldReturn400() throws Exception {
        Integer invalidFrom = -1;
        Integer invalidSize = 0;

        mockMvc.perform(get("/events")
                        .param("from", invalidFrom.toString())
                        .param("size", invalidSize.toString()))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventsPublic(any());
        verify(statistics, never()).postHit(any());
    }

    @Test
    void getEvent_ShouldReturn200AndEventFullDto() throws Exception {
        Long eventId = 1L;

        EventFullDto eventFullDto = EventFullDto.builder()
                .id(eventId)
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state("PUBLISHED")
                .build();

        when(eventService.getEventPublic(anyLong())).thenReturn(eventFullDto);
        doNothing().when(statistics).postHit(any());

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(validTitle))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(eventService, times(1)).getEventPublic(eventId);
        verify(statistics, times(1)).postHit(any());
    }

    @Test
    void getEvent_WithInvalidId_ShouldReturn400() throws Exception {
        Long invalidEventId = 0L;

        mockMvc.perform(get("/events/{id}", invalidEventId))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventPublic(anyLong());
        verify(statistics, never()).postHit(any());
    }
}