package ru.practicum.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.exception.conflict.EventNotEditableException;
import ru.practicum.main.exception.notFound.EventNotFoundException;
import ru.practicum.main.exception.validation.EventDateException;
import ru.practicum.main.exception.validation.ValidationException;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.implementations.EventServiceImpl;
import ru.practicum.main.stat.ConnectionToStatistics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServiceImplIntegrationTest {

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private ConnectionToStatistics statistics;

    private User user;
    private Category category;
    private Event event;
    private NewEventDto newEventDto;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        eventRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Создаем пользователя
        user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .build();
        user = userRepository.save(user);

        // Создаем категорию
        category = Category.builder()
                .name("Test Category")
                .build();
        category = categoryRepository.save(category);

        // Создаем событие
        event = Event.builder()
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .title("Test Event")
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .initiator(user)
                .category(category)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .publishedOn(null)
                .views(0L)
                .confirmedRequests(0L)
                .location(new ru.practicum.main.model.Location(55.7558f, 37.6173f))
                .build();
        event = eventRepository.save(event);

        // Подготавливаем NewEventDto
        newEventDto = NewEventDto.builder()
                .annotation("New Event Annotation")
                .description("New Event Description")
                .eventDate(LocalDateTime.now().plusDays(3))
                .title("New Event")
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .category(category.getId())
                .location(new LocationDto(55.7558f, 37.6173f))
                .build();

        // Настраиваем мок для статистики
        when(statistics.getViews(anyList())).thenReturn(Map.of());
    }

    @Test
    void createEvent_withInvalidEventDate_shouldThrowException() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1)); // Меньше 2 часов

        assertThrows(ValidationException.class, () -> eventService.createEvent(user.getId(), newEventDto));
    }

    @Test
    void getUserEvents_shouldReturnUserEvents() {
        List<EventShortDto> result = eventService.getUserEvents(user.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.get(0).getTitle());
    }

    @Test
    void getUserEvent_shouldReturnUserEvent() {
        EventFullDto result = eventService.getUserEvent(user.getId(), event.getId());

        assertEquals(event.getId(), result.getId());
        assertEquals(event.getTitle(), result.getTitle());
    }

    @Test
    void updateEventByUser_shouldUpdateEventSuccessfully() {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .annotation("Updated Annotation")
                .description("Updated Description")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(20)
                .build();

        EventFullDto result = eventService.updateEventByUser(user.getId(), event.getId(), updateRequest);

        assertEquals(updateRequest.getTitle(), result.getTitle());
        assertEquals(updateRequest.getAnnotation(), result.getAnnotation());
        assertEquals(updateRequest.getDescription(), result.getDescription());
        assertEquals(updateRequest.getPaid(), result.getPaid());
        assertEquals(updateRequest.getParticipantLimit(), result.getParticipantLimit());

        // Проверяем, что событие обновлено в БД
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(updateRequest.getTitle(), updatedEvent.getTitle());
    }

    @Test
    void updateEventByUser_withInvalidEventDate_shouldThrowException() {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .eventDate(LocalDateTime.now().plusHours(1)) // Меньше 2 часов
                .build();

        assertThrows(ValidationException.class, () ->
                eventService.updateEventByUser(user.getId(), event.getId(), updateRequest));
    }

    @Test
    void updateEventByUser_whenEventPublished_shouldThrowException() {
        // Меняем состояние события на PUBLISHED
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .build();

        assertThrows(EventNotEditableException.class, () ->
                eventService.updateEventByUser(user.getId(), event.getId(), updateRequest));
    }

    @Test
    void updateEventByAdmin_withInvalidEventDate_shouldThrowException() {
        // Публикуем событие
        event.setPublishedOn(LocalDateTime.now());
        eventRepository.save(event);

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .eventDate(LocalDateTime.now().plusMinutes(30)) // Меньше 1 часа после публикации
                .build();

        assertThrows(EventDateException.class, () -> eventService.updateEventByAdmin(event.getId(), updateRequest));
    }

    @Test
    void updateEventByAdmin_whenEventNotPending_shouldThrowException() {
        // Меняем состояние события на PUBLISHED
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction("PUBLISH_EVENT")
                .build();

        assertThrows(EventNotEditableException.class, () -> eventService.updateEventByAdmin(event.getId(), updateRequest));
    }

    @Test
    void getEventPublic_withNonPublishedEvent_shouldThrowException() {
        // Событие остается в состоянии PENDING

        assertThrows(EventNotFoundException.class, () -> eventService.getEventPublic(event.getId()));
    }

    @Test
    void getEventById_shouldReturnEvent() {
        Event result = eventService.getEventById(event.getId());

        assertEquals(event.getId(), result.getId());
    }

    @Test
    void getEventById_withNonExistentId_shouldThrowException() {
        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(999L));
    }

    @Test
    void updateEventByUser_withCategoryUpdate_shouldUpdateCategory() {
        // Создаем новую категорию
        Category newCategory = Category.builder()
                .name("New Category")
                .build();
        newCategory = categoryRepository.save(newCategory);

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .category(newCategory.getId())
                .build();

        EventFullDto result = eventService.updateEventByUser(user.getId(), event.getId(), updateRequest);

        assertEquals(newCategory.getId(), result.getCategory().getId());

        // Проверяем, что категория обновлена в БД
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(newCategory.getId(), updatedEvent.getCategory().getId());
    }
}