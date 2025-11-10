package ru.practicum.main.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.service.interfaces.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicEventControllerUnitTest {

    @Mock
    private EventService eventService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PublicEventController publicEventController;

    @Test
    void getEvents_ShouldReturnEventList() {
        // given
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder()
                .text("concert")
                .onlyAvailable(true)
                .from(0)
                .size(10)
                .build();

        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .build();

        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class), any(HttpServletRequest.class)))
                .thenReturn(List.of(eventShortDto));

        // when
        List<EventShortDto> result = publicEventController.getEvents(filter, request);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Summer Concert", result.get(0).getTitle());

        verify(eventService, times(1)).getEventsPublic(eq(filter), eq(request));
    }

    @Test
    void getEvents_WithDefaultParameters_ShouldReturnEventList() {
        // given
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder()
                .from(0)
                .size(10)
                .build();

        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class), any(HttpServletRequest.class)))
                .thenReturn(List.of());

        // when
        List<EventShortDto> result = publicEventController.getEvents(filter, request);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(eventService, times(1)).getEventsPublic(eq(filter), eq(request));
    }

    @Test
    void getEvent_ShouldReturnEvent() {
        // given
        Long eventId = 1L;

        EventFullDto expectedEvent = EventFullDto.builder()
                .id(eventId)
                .title("Summer Concert")
                .annotation("Great summer concert")
                .description("Detailed description of the summer concert")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state("PUBLISHED")
                .build();

        when(eventService.getEventPublic(anyLong(), any(HttpServletRequest.class)))
                .thenReturn(expectedEvent);

        // when
        EventFullDto actualEvent = publicEventController.getEvent(eventId, request);

        // then
        assertNotNull(actualEvent);
        assertEquals(eventId, actualEvent.getId());
        assertEquals("Summer Concert", actualEvent.getTitle());
        assertEquals("PUBLISHED", actualEvent.getState());

        verify(eventService, times(1)).getEventPublic(eq(eventId), eq(request));
    }

    @Test
    void getEvents_ShouldPassRequestParametersCorrectly() {
        // given
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder()
                .text("test")
                .categories(List.of(1L, 2L))
                .paid(true)
                .rangeStart(LocalDateTime.now().plusDays(1))
                .rangeEnd(LocalDateTime.now().plusDays(2))
                .onlyAvailable(false)
                .sort("EVENT_DATE")
                .from(5)
                .size(20)
                .build();

        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Test Event")
                .annotation("Test annotation")
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(true)
                .build();

        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class), any(HttpServletRequest.class)))
                .thenReturn(List.of(eventShortDto));

        // when
        List<EventShortDto> result = publicEventController.getEvents(filter, request);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(eventService, times(1)).getEventsPublic(eq(filter), eq(request));
    }
}