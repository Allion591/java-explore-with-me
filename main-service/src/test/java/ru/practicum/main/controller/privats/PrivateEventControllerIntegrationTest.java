package ru.practicum.main.controller.privats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.event.NewEventDto;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.service.interfaces.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PrivateEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private final Long userId = 1L;
    private final Long eventId = 1L;

    private final String validAnnotation = "This is a valid annotation that meets the minimum length requirement " +
            "of 20 characters";
    private final String validDescription = "This is a valid description that meets the minimum length requirement" +
            " of 20 characters and is long enough to pass validation";
    private final String validTitle = "Valid Event Title That Meets Requirements";

    @Test
    void getEvents_ShouldReturn200AndEventList() throws Exception {
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(eventId)
                .title(validTitle)
                .annotation(validAnnotation)
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(true)
                .build();

        when(eventService.getUserEvents(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(validTitle));

        verify(eventService, times(1)).getUserEvents(userId, 0, 10);
    }

    @Test
    void getEvents_WithInvalidPagination_ShouldReturn400() throws Exception {
        Integer invalidFrom = -1;
        Integer invalidSize = 0;

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", invalidFrom.toString())
                        .param("size", invalidSize.toString()))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getUserEvents(anyLong(), anyInt(), anyInt());
    }

    @Test
    void createEvent_WithPastEventDate_ShouldReturn400() throws Exception {
        //- дата в прошлом
        NewEventDto invalidRequest = NewEventDto.builder()
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .category(1L)
                .eventDate(LocalDateTime.now().minusDays(1)) // Прошлая дата
                .location(new LocationDto(55.7558f, 37.6173f))
                .build();

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(anyLong(), any());
    }

    @Test
    void getEvent_ShouldReturn200AndEventFullDto() throws Exception {
        EventFullDto response = EventFullDto.builder()
                .id(eventId)
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .state("PUBLISHED")
                .build();

        when(eventService.getUserEvent(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(validTitle))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(eventService, times(1)).getUserEvent(userId, eventId);
    }

    @Test
    void getEvent_WithInvalidIds_ShouldReturn400() throws Exception {
        // - невалидные ID
        Long invalidUserId = 0L;
        Long invalidEventId = 0L;

        mockMvc.perform(get("/users/{userId}/events/{eventId}", invalidUserId, invalidEventId))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getUserEvent(anyLong(), anyLong());
    }
}