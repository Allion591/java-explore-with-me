package ru.practicum.main.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user1, user2;
    private Category category1, category2;
    private Event event1, event2, event3;

    @BeforeEach
    void setUp() {
        // Создаем пользователей
        user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@email.com");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@email.com");
        user2 = userRepository.save(user2);

        // Создаем категории
        category1 = new Category();
        category1.setName("Category 1");
        category1 = categoryRepository.save(category1);

        category2 = new Category();
        category2.setName("Category 2");
        category2 = categoryRepository.save(category2);

        // Создаем локацию
        Location location = new Location(55.7558f, 37.6173f);

        // Создаем события
        event1 = Event.builder()
                .annotation("Test annotation 1")
                .category(category1)
                .description("Test description 1")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(location)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .title("Test Event 1")
                .initiator(user1)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .build();

        event2 = Event.builder()
                .annotation("Test annotation 2")
                .category(category2)
                .description("Test description 2")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(location)
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .title("Test Event 2")
                .initiator(user2)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .build();

        event3 = Event.builder()
                .annotation("Another annotation")
                .category(category1)
                .description("Another description")
                .eventDate(LocalDateTime.now().plusDays(3))
                .location(location)
                .paid(true)
                .participantLimit(5)
                .requestModeration(true)
                .title("Another Event")
                .initiator(user1)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .build();

        eventRepository.saveAll(List.of(event1, event2, event3));
    }

    @Test
    void findByInitiatorId_shouldReturnUserEvents() {
        Page<Event> result = eventRepository.findByInitiatorId(
                user1.getId(), PageRequest.of(0, 10)
        );

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(event -> event.getInitiator().getId().equals(user1.getId())));
    }

    @Test
    void findByIdAndInitiatorId_whenExists_shouldReturnEvent() {
        Optional<Event> result = eventRepository.findByIdAndInitiatorId(
                event1.getId(), user1.getId()
        );

        assertTrue(result.isPresent());
        assertEquals(event1.getId(), result.get().getId());
    }

    @Test
    void findByIdAndInitiatorId_whenNotExists_shouldReturnEmpty() {
        Optional<Event> result = eventRepository.findByIdAndInitiatorId(
                event1.getId(), user2.getId()
        );

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdIn_shouldReturnEventsByIds() {
        List<Event> result = eventRepository.findByIdIn(
                List.of(event1.getId(), event3.getId())
        );

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(event -> event.getId().equals(event1.getId())));
        assertTrue(result.stream()
                .anyMatch(event -> event.getId().equals(event3.getId())));
    }

    @Test
    void findByIdAndState_whenPublishedEvent_shouldReturnEvent() {
        Optional<Event> result = eventRepository.findByIdAndState(
                event1.getId(), EventState.PUBLISHED
        );

        assertTrue(result.isPresent());
        assertEquals(event1.getId(), result.get().getId());
    }

    @Test
    void findByIdAndState_whenNotPublished_shouldReturnEmpty() {
        Optional<Event> result = eventRepository.findByIdAndState(
                event2.getId(), EventState.PUBLISHED
        );

        assertFalse(result.isPresent());
    }

    @Test
    void findByCategoryId_shouldReturnCategoryEvents() {
        List<Event> result = eventRepository.findByCategoryId(category1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(event -> event.getCategory().getId().equals(category1.getId())));
    }

    @Test
    void existsByIdAndInitiatorId_whenExists_shouldReturnTrue() {
        boolean exists = eventRepository.existsByIdAndInitiatorId(
                event1.getId(), user1.getId()
        );

        assertTrue(exists);
    }

    @Test
    void existsByIdAndInitiatorId_whenNotExists_shouldReturnFalse() {
        boolean exists = eventRepository.existsByIdAndInitiatorId(
                event1.getId(), user2.getId()
        );

        assertFalse(exists);
    }

    @Test
    void findPublishedEventsByIds_shouldReturnOnlyPublishedEvents() {
        List<Event> result = eventRepository.findPublishedEventsByIds(
                List.of(event1.getId(), event2.getId(), event3.getId())
        );

        assertEquals(2, result.size()); // event2 не опубликован
        assertTrue(result.stream()
                .allMatch(event -> event.getState() == EventState.PUBLISHED));
    }
}