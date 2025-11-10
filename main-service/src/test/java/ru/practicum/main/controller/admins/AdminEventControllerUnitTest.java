package ru.practicum.main.controller.admins;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.service.interfaces.EventService;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventControllerUnitTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private AdminEventController adminEventController;

    @Test
    void getEvents_ShouldReturnEventList() {
        AdminEventFilterParams filterParams = AdminEventFilterParams.builder()
                .users(List.of(1L))
                .states(List.of(EventState.PUBLISHED))
                .categories(List.of(1L))
                .build();

        EventFullDto eventDto = EventFullDto.builder()
                .id(1L)
                .title("Test Event")
                .build();

        when(eventService.getEventsByAdmin(any(AdminEventFilterParams.class)))
                .thenReturn(List.of(eventDto));

        List<EventFullDto> result = adminEventController.getEvents(filterParams);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(eventService, times(1)).getEventsByAdmin(any(AdminEventFilterParams.class));
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() {
        Long eventId = 1L;
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Updated Title")
                .build();

        EventFullDto expectedResponse = EventFullDto.builder()
                .id(1L)
                .title("Updated Title")
                .state("PUBLISHED")
                .build();

        when(eventService.updateEventByAdmin(anyLong(), any(UpdateEventAdminRequest.class)))
                .thenReturn(expectedResponse);

        EventFullDto actualResponse = adminEventController.updateEvent(eventId, request);

        assertNotNull(actualResponse);
        assertEquals("Updated Title", actualResponse.getTitle());
        assertEquals("PUBLISHED", actualResponse.getState());
        verify(eventService, times(1)).updateEventByAdmin(anyLong(), any(UpdateEventAdminRequest.class));
    }
}