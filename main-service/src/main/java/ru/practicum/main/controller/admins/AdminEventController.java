package ru.practicum.main.controller.admins;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.service.interfaces.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@Valid @ModelAttribute AdminEventFilterParams filterParams) {
        log.info("Admin: поиск событий с параметрами - users: {}, states: {}, categories: {}",
                filterParams.getUsers(), filterParams.getStates(), filterParams.getCategories());
        return eventService.getEventsByAdmin(filterParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Min(1) Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Admin: обновление события id={} (статус: {})",
                eventId, updateEventAdminRequest.getStateAction());
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
    }
}