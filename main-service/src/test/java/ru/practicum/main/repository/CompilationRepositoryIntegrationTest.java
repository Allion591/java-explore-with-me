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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CompilationRepositoryIntegrationTest {

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Compilation compilation1;
    private Compilation compilation2;
    private Event event;
    private Location location;

    @BeforeEach
    void setUp() {
        // Создаем пользователя и категорию для события
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@email.com");
        user = userRepository.save(user);

        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        location = new Location();
        location.setLat(55.7558F);
        location.setLon(37.6173F);

        // Создаем событие
        event = new Event();
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setState(EventState.PUBLISHED);
        event.setLocation(location);
        event = eventRepository.save(event);

        // Создаем подборки
        compilation1 = new Compilation();
        compilation1.setTitle("Compilation 1");
        compilation1.setPinned(true);
        compilation1.setEvents(Set.of(event));

        compilation2 = new Compilation();
        compilation2.setTitle("Compilation 2");
        compilation2.setPinned(false);

        compilationRepository.saveAll(List.of(compilation1, compilation2));
    }

    @Test
    void findByPinned_shouldReturnPinnedCompilations() {
        Page<Compilation> result = compilationRepository.findByPinned(
                true, PageRequest.of(0, 10)
        );

        assertEquals(1, result.getContent().size());
        assertEquals("Compilation 1", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getPinned());
    }

    @Test
    void findAll_withPageable_shouldReturnAllCompilations() {
        Page<Compilation> result = compilationRepository.findAll(
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getContent().size());
    }

    @Test
    void existsByTitle_whenTitleExists_shouldReturnTrue() {
        boolean exists = compilationRepository.existsByTitle("Compilation 1");

        assertTrue(exists);
    }

    @Test
    void existsByTitle_whenTitleNotExists_shouldReturnFalse() {
        boolean exists = compilationRepository.existsByTitle("Non Existing");

        assertFalse(exists);
    }

    @Test
    void findByTitle_whenTitleExists_shouldReturnCompilation() {
        Optional<Compilation> result = compilationRepository.findByTitle("Compilation 1");

        assertTrue(result.isPresent());
        assertEquals("Compilation 1", result.get().getTitle());
    }

    @Test
    void findByTitle_whenTitleNotExists_shouldReturnEmpty() {
        Optional<Compilation> result = compilationRepository.findByTitle("Non Existing");

        assertFalse(result.isPresent());
    }

    @Test
    void findByEventId_shouldReturnCompilationsContainingEvent() {
        List<Compilation> result = compilationRepository.findByEventId(event.getId());

        assertEquals(1, result.size());
        assertEquals("Compilation 1", result.get(0).getTitle());
        assertTrue(result.get(0).getEvents().contains(event));
    }

    @Test
    void findByEventId_whenNoCompilationsContainEvent_shouldReturnEmptyList() {
        Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setAnnotation("New Annotation");
        newEvent.setDescription("New Description");

        User user = userRepository.findAll().get(0);
        Category category = categoryRepository.findAll().get(0);

        newEvent.setCategory(category);
        newEvent.setInitiator(user);
        newEvent.setEventDate(LocalDateTime.now().plusDays(2));
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setPaid(false);
        newEvent.setParticipantLimit(0);
        newEvent.setRequestModeration(true);
        newEvent.setState(EventState.PUBLISHED);
        newEvent.setLocation(location);

        Event savedEvent = eventRepository.save(newEvent);

        List<Compilation> result = compilationRepository.findByEventId(savedEvent.getId());

        assertTrue(result.isEmpty());
    }
}