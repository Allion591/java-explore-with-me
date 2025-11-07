package ru.practicum.main.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.service.interfaces.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    void getEvents_ShouldReturn200AndEventList() throws Exception {
        EventFullDto eventDto = EventFullDto.builder()
                .id(1L)
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state("PUBLISHED")
                .build();

        when(eventService.getEventsByAdmin(any(AdminEventFilterParams.class)))
                .thenReturn(List.of(eventDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1")
                        .param("states", "PUBLISHED")
                        .param("categories", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Event"))
                .andExpect(jsonPath("$[0].state").value("PUBLISHED"));

        verify(eventService, times(1)).getEventsByAdmin(any(AdminEventFilterParams.class));
    }

    @Test
    void getEvents_WithInvalidDateRange_ShouldReturn400() throws Exception {
        // Given - невалидный диапазон дат (start после end)
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/admin/events")
                        .param("rangeStart", start.toString())
                        .param("rangeEnd", end.toString()))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventsByAdmin(any());
    }

    @Test
    void updateEvent_ShouldReturn200AndUpdatedEvent() throws Exception {
        Long eventId = 1L;
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Updated Event Title")
                .annotation("This is a valid annotation that meets the minimum length requirement of 20 characters")
                .description("This is a valid description that meets the minimum length requirement of 20 characters" +
                        " and is long enough to pass validation")
                .eventDate(LocalDateTime.now().plusDays(2))
                .paid(true)
                .participantLimit(50)
                .stateAction("PUBLISH_EVENT")
                .build();

        EventFullDto response = EventFullDto.builder()
                .id(1L)
                .title("Updated Event Title")
                .state("PUBLISHED")
                .build();

        when(eventService.updateEventByAdmin(anyLong(), any(UpdateEventAdminRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Event Title"))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(eventService, times(1)).updateEventByAdmin(anyLong(),
                any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEvent_WithInvalidId_ShouldReturn400() throws Exception {
        Long invalidEventId = 0L;
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Valid Title")
                .build();

        mockMvc.perform(patch("/admin/events/{eventId}", invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByAdmin(anyLong(), any());
    }

    @Test
    void updateEvent_WithInvalidData_ShouldReturn400() throws Exception {
        Long eventId = 1L;
        // Невалидные данные - слишком короткий заголовок
        UpdateEventAdminRequest invalidRequest = UpdateEventAdminRequest.builder()
                .title("A") // Меньше 3 символов
                .build();

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByAdmin(anyLong(), any());
    }

    @Test
    void updateEvent_WithPastEventDate_ShouldReturn400() throws Exception {
        Long eventId = 1L;
        // Дата в прошлом - невалидно
        UpdateEventAdminRequest invalidRequest = UpdateEventAdminRequest.builder()
                .title("Valid Title")
                .eventDate(LocalDateTime.now().minusDays(1)) // Прошлая дата
                .build();

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByAdmin(anyLong(), any());
    }
}