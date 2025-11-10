package ru.practicum.main.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.model.Event;

import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getEventsByAdmin(AdminEventFilterParams filterParams);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    Event getEventById(Long eventId);

    List<EventShortDto> getEventsPublic(EventPublicFilterRequest filter, HttpServletRequest request);

    EventFullDto getEventPublic(Long eventId, HttpServletRequest request);
}