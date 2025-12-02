package ru.practicum.main.service.implementations;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.exception.conflict.EventNotEditableException;
import ru.practicum.main.exception.database.DataRetrievalException;
import ru.practicum.main.exception.notFound.EventNotFoundException;
import ru.practicum.main.exception.validation.EventDateException;
import ru.practicum.main.exception.validation.ValidationException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.service.interfaces.CategoryService;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.UserService;
import ru.practicum.stats.statsClient.StatsClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание нового события для пользователя: {}", userId);

        User user = userService.getUserById(userId);

        // Проверка даты события (минимум 2 часа от текущего момента)
        validateEventDate(newEventDto.getEventDate());

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(categoryService.getCategoryById(newEventDto.getCategory()));

        Event savedEvent = eventRepository.save(event);

        log.info("Событие создано id: {} для пользователя: {}", savedEvent.getId(), userId);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событии для пользователя: {}, from: {}, size: {}", userId, from, size);

        userService.checkUserExists(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события: {} для пользователя: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события: {} пользователем: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Проверка, что событие можно редактировать
        if (event.getState() == EventState.PUBLISHED) {
            throw new EventNotEditableException("Можно изменить только отложенные или отмененные события");
        }

        // Проверка даты события при обновлении
        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate());
        }

        eventMapper.updateEventFromUserRequest(updateRequest, event);

        // Обновление категории, если указана
        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateRequest.getCategory()));
        }

        Event updatedEvent = eventRepository.save(event);

        log.info("Событие: {} обновлено пользователем: {}", eventId, userId);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(AdminEventFilterParams filterParams) {
        log.info("Получение событий администратором с помощью фильтров");

        Pageable pageable = PageRequest.of(
                filterParams.getFrom() / filterParams.getSize(),
                filterParams.getSize(),
                Sort.by("id").ascending()
        );

        Page<Object[]> results = eventRepository.findEventsByAdminWithCounts(filterParams, pageable);

        List<Event> events = results.getContent().stream()
                .map(result -> {
                    Event event = (Event) result[0];
                    Long confirmedRequests = (Long) result[1];
                    event.setConfirmedRequests(confirmedRequests);
                    return event;
                })
                .toList();

        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события: {} администратором", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Проверка даты публикации
        if (updateRequest.getEventDate() != null) {
            validateEventDateForAdmin(updateRequest.getEventDate(), event.getPublishedOn());
        }

        // Проверка состояния для публикации
        if (updateRequest.getStateAction() != null &&
                updateRequest.getStateAction().equals("PUBLISH_EVENT") &&
                event.getState() != EventState.PENDING) {
            throw new EventNotEditableException(
                    "Не удается опубликовать событие, потому что оно находится в неправильном состоянии: "
                            + event.getState());
        }

        // Проверка состояния для отклонения
        if (updateRequest.getStateAction() != null &&
                updateRequest.getStateAction().equals("REJECT_EVENT") &&
                event.getState() == EventState.PUBLISHED) {
            throw new EventNotEditableException("Невозможно отклонить событие, поскольку оно уже опубликовано");
        }

        eventMapper.updateEventFromAdminRequest(updateRequest, event);

        // Обновление категории, если указана
        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateRequest.getCategory()));
        }

        Event updatedEvent = eventRepository.save(event);

        log.info("Событие: {} обновлено администратором", eventId);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsPublic(EventPublicFilterRequest filter, HttpServletRequest request) {
        log.info("Получение событий с фильтрами: {}", filter);

        // Записываем просмотр
        statsClient.recordHit(request.getRequestURI(), request.getRemoteAddr());

        // Подготовка фильтра
        filter.getEffectiveRangeStart();

        // Определение сортировки
        Sort sorting = Sort.by("eventDate").ascending();
        if ("VIEWS".equals(filter.getSort())) {
            sorting = Sort.by("views").descending();
        }

        Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize(), sorting);

        try {
            Page<Event> events = eventRepository.findEventsByPublic(filter, pageable);

            log.info("Найдено {} событий", events.getNumberOfElements());
            List<EventShortDto> eventDtos = events.getContent()
                    .stream()
                    .map(eventMapper::toEventShortDto)
                    .collect(Collectors.toList());

            // Получаем просмотры из сервиса статистики
            enrichWithViews(eventDtos);

            // Если сортировка по просмотрам, сортируем после получения статистики
            if ("VIEWS".equals(filter.getSort())) {
                eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }

            log.info("Найдено {} событий", events.getNumberOfElements());
            return eventDtos;

        } catch (Exception e) {
            log.error("Ошибка при получении событий: {}", e.getMessage(), e);
            throw new DataRetrievalException("Не удалось получить события");
        }
    }

    @Override
    public EventFullDto getEventPublic(Long eventId, HttpServletRequest request) {
        log.info("Получение опубликованного события: {}", eventId);

        // Записываем просмотр
        statsClient.recordHit(request.getRequestURI(), request.getRemoteAddr());

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        EventFullDto eventDto = eventMapper.toEventFullDto(event);

        // Получаем просмотры из сервиса статистики
        enrichWithViews(eventDto);

        return eventDto;
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: {}", eventDate);
            throw new ValidationException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + eventDate);
        }
    }

    private void validateEventDateForAdmin(LocalDateTime eventDate, LocalDateTime publishedOn) {
        if (publishedOn != null && eventDate.isBefore(publishedOn.plusHours(1))) {
            log.error("Дата мероприятия должна быть не позднее, чем через 1 час после даты публикации");
            throw new EventDateException(
                    "Дата мероприятия должна быть не позднее, чем через 1 час после даты публикации"
            );
        }
    }

    @Override
    public Event getEventById(Long eventId) {
        log.info("Получение события: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Override
    public void existsById(Long id) {
        if (!eventRepository.existsById(id)) {
            log.error("Событие с id: {} не найдено", id);
            throw new EventNotFoundException(id);
        }
    }

    private void enrichWithViews(Object eventOrEvents) {
        if (eventOrEvents == null) return;

        Set<Long> eventIds;

        if (eventOrEvents instanceof EventFullDto event) {
            eventIds = Set.of(event.getId());
        } else if (eventOrEvents instanceof List<?> events && !events.isEmpty()) {
            eventIds = events.stream()
                    .filter(e -> e instanceof EventShortDto)
                    .map(e -> ((EventShortDto) e).getId())
                    .collect(Collectors.toSet());
        } else {
            return;
        }

        Map<Long, Long> views = statsClient.getEventsViews(eventIds, true);

        if (eventOrEvents instanceof EventFullDto event) {
            event.setViews(views.getOrDefault(event.getId(), 0L));
        } else if (eventOrEvents instanceof List<?> events) {
            events.forEach(e -> {
                if (e instanceof EventShortDto eventShort) {
                    eventShort.setViews(views.getOrDefault(eventShort.getId(), 0L));
                }
            });
        }
    }
}