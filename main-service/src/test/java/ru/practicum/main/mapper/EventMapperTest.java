package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.model.Event;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventMapperTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    void toEventShortDto_ShouldMapEventToEventShortDto() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .confirmedRequests(50L)
                .views(100L)
                .build();

        // When
        EventShortDto eventShortDto = eventMapper.toEventShortDto(event);

        // Then
        assertNotNull(eventShortDto);
        assertEquals(event.getId(), eventShortDto.getId());
        assertEquals(event.getTitle(), eventShortDto.getTitle());
        assertEquals(event.getAnnotation(), eventShortDto.getAnnotation());
        assertEquals(event.getEventDate(), eventShortDto.getEventDate());
        assertEquals(event.getPaid(), eventShortDto.getPaid());
        assertEquals(event.getConfirmedRequests(), eventShortDto.getConfirmedRequests());
        assertEquals(event.getViews(), eventShortDto.getViews());
    }

    @Test
    void toEventFullDto_ShouldMapEventToEventFullDto() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert")
                .description("Detailed description")
                .eventDate(LocalDateTime.now().plusDays(5))
                .createdOn(LocalDateTime.now().minusDays(1))
                .publishedOn(LocalDateTime.now())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .confirmedRequests(50L)
                .views(100L)
                .build();

        // When
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        // Then
        assertNotNull(eventFullDto);
        assertEquals(event.getId(), eventFullDto.getId());
        assertEquals(event.getTitle(), eventFullDto.getTitle());
        assertEquals(event.getAnnotation(), eventFullDto.getAnnotation());
        assertEquals(event.getDescription(), eventFullDto.getDescription());
        assertEquals(event.getEventDate(), eventFullDto.getEventDate());
        assertEquals(event.getCreatedOn(), eventFullDto.getCreatedOn());
        assertEquals(event.getPublishedOn(), eventFullDto.getPublishedOn());
        assertEquals(event.getPaid(), eventFullDto.getPaid());
        assertEquals(event.getParticipantLimit(), eventFullDto.getParticipantLimit());
        assertEquals(event.getRequestModeration(), eventFullDto.getRequestModeration());
        assertEquals(event.getState().name(), eventFullDto.getState());
        assertEquals(event.getConfirmedRequests(), eventFullDto.getConfirmedRequests());
        assertEquals(event.getViews(), eventFullDto.getViews());
    }

    @Test
    void toEvent_ShouldMapNewEventDtoToEvent() {
        // Given
        NewEventDto newEventDto = NewEventDto.builder()
                .title("New Event")
                .annotation("Event annotation")
                .description("Event description")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(50)
                .requestModeration(false)
                .build();

        // When
        Event event = eventMapper.toEvent(newEventDto);

        // Then
        assertNotNull(event);
        assertNull(event.getId()); // id должен быть проигнорирован
        assertEquals(newEventDto.getTitle(), event.getTitle());
        assertEquals(newEventDto.getAnnotation(), event.getAnnotation());
        assertEquals(newEventDto.getDescription(), event.getDescription());
        assertEquals(newEventDto.getEventDate(), event.getEventDate());
        assertEquals(newEventDto.getPaid(), event.getPaid());
        assertEquals(newEventDto.getParticipantLimit(), event.getParticipantLimit());
        assertEquals(newEventDto.getRequestModeration(), event.getRequestModeration());
        assertEquals(EventState.PENDING, event.getState()); // состояние должно быть PENDING
        assertNotNull(event.getCreatedOn()); // createdOn должен быть установлен
        assertNull(event.getPublishedOn()); // publishedOn должен быть проигнорирован
    }

    @Test
    void updateEventFromUserRequest_ShouldUpdateFieldsAndHandleStateAction() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Old Title")
                .annotation("Old Annotation")
                .state(EventState.PENDING)
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("New Title")
                .stateAction("SEND_TO_REVIEW")
                .build();

        // When
        eventMapper.updateEventFromUserRequest(updateRequest, event);

        // Then
        assertEquals(1L, event.getId()); // id не должен измениться
        assertEquals("New Title", event.getTitle());
        assertEquals(EventState.PENDING, event.getState()); // остается PENDING для SEND_TO_REVIEW
    }

    @Test
    void updateEventFromUserRequest_ShouldCancelEvent() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Event")
                .state(EventState.PENDING)
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .stateAction("CANCEL_REVIEW")
                .build();

        // When
        eventMapper.updateEventFromUserRequest(updateRequest, event);

        // Then
        assertEquals(EventState.CANCELED, event.getState());
    }

    @Test
    void updateEventFromAdminRequest_ShouldPublishEvent() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Event")
                .state(EventState.PENDING)
                .publishedOn(null)
                .build();

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction("PUBLISH_EVENT")
                .build();

        // When
        eventMapper.updateEventFromAdminRequest(updateRequest, event);

        // Then
        assertEquals(EventState.PUBLISHED, event.getState());
        assertNotNull(event.getPublishedOn()); // publishedOn должен быть установлен
    }

    @Test
    void updateEventFromAdminRequest_ShouldRejectEvent() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Event")
                .state(EventState.PENDING)
                .build();

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction("REJECT_EVENT")
                .build();

        // When
        eventMapper.updateEventFromAdminRequest(updateRequest, event);

        // Then
        assertEquals(EventState.CANCELED, event.getState());
    }

    @Test
    void updateEventFromUserRequest_ShouldIgnoreNullFields() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .title("Original Title")
                .annotation("Original Annotation")
                .state(EventState.PENDING)
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title(null) // null поле
                .stateAction("SEND_TO_REVIEW")
                .build();

        // When
        eventMapper.updateEventFromUserRequest(updateRequest, event);

        // Then - title не должен измениться
        assertEquals("Original Title", event.getTitle());
        assertEquals(EventState.PENDING, event.getState());
    }

    @Test
    void toEventShortDto_ShouldHandleNull() {
        // When
        EventShortDto eventShortDto = eventMapper.toEventShortDto(null);

        // Then
        assertNull(eventShortDto);
    }

    @Test
    void toEventFullDto_ShouldHandleNull() {
        // When
        EventFullDto eventFullDto = eventMapper.toEventFullDto(null);

        // Then
        assertNull(eventFullDto);
    }

    @Test
    void toEvent_ShouldHandleNull() {
        // When
        Event event = eventMapper.toEvent(null);

        // Then
        assertNull(event);
    }
}