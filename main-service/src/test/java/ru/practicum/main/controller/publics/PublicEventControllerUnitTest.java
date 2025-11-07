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
import ru.practicum.main.stat.ConnectionToStatistics;

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
    private ConnectionToStatistics statistics;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PublicEventController publicEventController;

    @Test
    void getEvents_ShouldReturnEventListAndPostHit() {
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

        when(eventService.getEventsPublic(any(EventPublicFilterRequest.class)))
                .thenReturn(List.of(eventShortDto));

        doNothing().when(statistics).postHit(any(HttpServletRequest.class));

        List<EventShortDto> result = publicEventController.getEvents(filter, request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Summer Concert", result.get(0).getTitle());

        verify(eventService, times(1)).getEventsPublic(filter);
        verify(statistics, times(1)).postHit(request);
    }

    @Test
    void getEvent_ShouldReturnEventAndPostHit() {
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

        when(eventService.getEventPublic(anyLong())).thenReturn(expectedEvent);
        doNothing().when(statistics).postHit(any(HttpServletRequest.class));

        EventFullDto actualEvent = publicEventController.getEvent(eventId, request);

        assertNotNull(actualEvent);
        assertEquals(eventId, actualEvent.getId());
        assertEquals("Summer Concert", actualEvent.getTitle());
        assertEquals("PUBLISHED", actualEvent.getState());

        verify(eventService, times(1)).getEventPublic(eventId);
        verify(statistics, times(1)).postHit(request);
    }
}