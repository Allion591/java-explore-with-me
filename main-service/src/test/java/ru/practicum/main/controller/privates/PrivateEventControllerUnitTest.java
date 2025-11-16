package ru.practicum.main.controller.privates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.event.NewEventDto;
import ru.practicum.main.dto.event.UpdateEventUserRequest;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.service.interfaces.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateEventControllerUnitTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private PrivateEventController privateEventController;

    private final Long userId = 1L;
    private final Long eventId = 1L;

    @Test
    void getEvents_ShouldReturnEventList() {
        Integer from = 0;
        Integer size = 10;

        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Test Event")
                .build();

        when(eventService.getUserEvents(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        List<EventShortDto> result = privateEventController.getEvents(userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.getFirst().getTitle());
        verify(eventService, times(1)).getUserEvents(userId, from, size);
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        NewEventDto request = NewEventDto.builder()
                .title("New Event")
                .annotation("This is a valid annotation that meets length requirements")
                .description("This is a valid description that meets length requirements")
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new LocationDto(55.7558f, 37.6173f))
                .build();

        EventFullDto expectedResponse = EventFullDto.builder()
                .id(1L)
                .title("New Event")
                .state("PENDING")
                .build();

        when(eventService.createEvent(anyLong(), any(NewEventDto.class))).thenReturn(expectedResponse);

        EventFullDto actualResponse = privateEventController.createEvent(userId, request);

        assertNotNull(actualResponse);
        assertEquals("New Event", actualResponse.getTitle());
        assertEquals("PENDING", actualResponse.getState());
        verify(eventService, times(1)).createEvent(userId, request);
    }

    @Test
    void getEvent_ShouldReturnEvent() {
        EventFullDto expectedResponse = EventFullDto.builder()
                .id(eventId)
                .title("Test Event")
                .state("PUBLISHED")
                .build();

        when(eventService.getUserEvent(anyLong(), anyLong())).thenReturn(expectedResponse);

        EventFullDto actualResponse = privateEventController.getEvent(userId, eventId);

        assertNotNull(actualResponse);
        assertEquals(eventId, actualResponse.getId());
        assertEquals("PUBLISHED", actualResponse.getState());
        verify(eventService, times(1)).getUserEvent(userId, eventId);
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() {
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .build();

        EventFullDto expectedResponse = EventFullDto.builder()
                .id(eventId)
                .title("Updated Title")
                .state("PENDING")
                .build();

        when(eventService.updateEventByUser(anyLong(), anyLong(), any(UpdateEventUserRequest.class)))
                .thenReturn(expectedResponse);

        EventFullDto actualResponse = privateEventController.updateEvent(userId, eventId, request);

        assertNotNull(actualResponse);
        assertEquals("Updated Title", actualResponse.getTitle());
        verify(eventService, times(1)).updateEventByUser(userId, eventId, request);
    }
}