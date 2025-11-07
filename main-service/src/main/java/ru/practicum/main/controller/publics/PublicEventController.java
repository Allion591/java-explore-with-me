package ru.practicum.main.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.stat.ConnectionToStatistics;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final ConnectionToStatistics statistics;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid @ModelAttribute EventPublicFilterRequest filter,
                                         HttpServletRequest request) {
        log.info("Public: получение событий - текст: '{}', категории: {}, только доступные: {}",
                filter.getText() != null ? filter.getText().substring(0,
                        Math.min(50, filter.getText().length())) : "нет",
                filter.getCategories() != null ? filter.getCategories().size() : 0,
                filter.getOnlyAvailable());

        statistics.postHit(request);

        return eventService.getEventsPublic(filter);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable @Min(1) Long id, HttpServletRequest request) {
        log.info("Public: получение события id={}", id);

        statistics.postHit(request);

        return eventService.getEventPublic(id);
    }
}