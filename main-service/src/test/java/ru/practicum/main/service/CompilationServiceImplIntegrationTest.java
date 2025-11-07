package ru.practicum.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.exception.notFound.CompilationNotFoundException;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.implementations.CompilationServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompilationServiceImplIntegrationTest {

    @Autowired
    private CompilationServiceImpl compilationService;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Category category;
    private Event event1, event2;
    private Compilation existingCompilation;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Создаем тестового пользователя
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

        // Создаем события
        event1 = Event.builder()
                .annotation("Test Annotation 1")
                .description("Test Description 1")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Test Event 1")
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .initiator(user)
                .category(category)
                .state(ru.practicum.main.enums.EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .location(new ru.practicum.main.model.Location(55.7558f, 37.6173f))
                .build();
        event1 = eventRepository.save(event1);

        event2 = Event.builder()
                .annotation("Test Annotation 2")
                .description("Test Description 2")
                .eventDate(LocalDateTime.now().plusDays(2))
                .title("Test Event 2")
                .paid(true)
                .participantLimit(10)
                .requestModeration(false)
                .initiator(user)
                .category(category)
                .state(ru.practicum.main.enums.EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .location(new ru.practicum.main.model.Location(55.7558f, 37.6173f))
                .build();
        event2 = eventRepository.save(event2);

        // Создаем существующую подборку
        existingCompilation = Compilation.builder()
                .title("Existing Compilation")
                .pinned(true)
                .events(Set.of(event1))
                .build();
        existingCompilation = compilationRepository.save(existingCompilation);
    }

    @Test
    void createCompilation_shouldCreateCompilationSuccessfully() {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title("New Compilation")
                .pinned(false)
                .events(List.of(event2.getId()))
                .build();

        CompilationDto result = compilationService.createCompilation(newCompilationDto);

        assertNotNull(result.getId());
        assertEquals("New Compilation", result.getTitle());
        assertFalse(result.getPinned());
        assertEquals(1, result.getEvents().size());

        // Проверяем, что подборка сохранена в БД
        Compilation savedCompilation = compilationRepository.findById(result.getId()).orElseThrow();
        assertEquals("New Compilation", savedCompilation.getTitle());
    }

    @Test
    void createCompilation_withoutEvents_shouldCreateEmptyCompilation() {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title("Empty Compilation")
                .pinned(true)
                .events(null)
                .build();

        CompilationDto result = compilationService.createCompilation(newCompilationDto);

        assertNotNull(result.getId());
        assertEquals("Empty Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertTrue(result.getEvents().isEmpty());
    }

    @Test
    void deleteCompilation_shouldDeleteCompilationSuccessfully() {
        compilationService.deleteCompilation(existingCompilation.getId());

        assertFalse(compilationRepository.existsById(existingCompilation.getId()));
    }

    @Test
    void deleteCompilation_withNonExistentId_shouldThrowException() {
        assertThrows(CompilationNotFoundException.class, () -> {
            compilationService.deleteCompilation(999L);
        });
    }

    @Test
    void updateCompilation_shouldUpdateCompilationSuccessfully() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Updated Compilation")
                .pinned(false)
                .events(List.of(event1.getId(), event2.getId()))
                .build();

        CompilationDto result = compilationService.updateCompilation(existingCompilation.getId(), updateRequest);

        assertEquals(existingCompilation.getId(), result.getId());
        assertEquals("Updated Compilation", result.getTitle());
        assertFalse(result.getPinned());
        assertEquals(2, result.getEvents().size());

        // Проверяем, что подборка обновлена в БД
        Compilation updatedCompilation = compilationRepository.findById(existingCompilation.getId()).orElseThrow();
        assertEquals("Updated Compilation", updatedCompilation.getTitle());
        assertFalse(updatedCompilation.getPinned());
        assertEquals(2, updatedCompilation.getEvents().size());
    }

    @Test
    void updateCompilation_withEventsUpdate_shouldUpdateEvents() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .events(List.of(event2.getId()))
                .build();

        CompilationDto result = compilationService.updateCompilation(existingCompilation.getId(), updateRequest);

        assertEquals(1, result.getEvents().size());
        assertEquals(event2.getId(), result.getEvents().get(0).getId());
    }

    @Test
    void updateCompilation_withNonExistentId_shouldThrowException() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Updated Title")
                .build();

        assertThrows(CompilationNotFoundException.class, () -> {
            compilationService.updateCompilation(999L, updateRequest);
        });
    }

    @Test
    void getCompilations_withPinnedFilter_shouldReturnFilteredCompilations() {
        // Создаем еще одну подборку (не закрепленную)
        Compilation unpinnedCompilation = Compilation.builder()
                .title("Unpinned Compilation")
                .pinned(false)
                .events(Set.of(event2))
                .build();
        compilationRepository.save(unpinnedCompilation);

        List<CompilationDto> pinnedResult = compilationService.getCompilations(true, 0, 10);
        List<CompilationDto> unpinnedResult = compilationService.getCompilations(false, 0, 10);

        assertEquals(1, pinnedResult.size());
        assertEquals("Existing Compilation", pinnedResult.get(0).getTitle());

        assertEquals(1, unpinnedResult.size());
        assertEquals("Unpinned Compilation", unpinnedResult.get(0).getTitle());
    }

    @Test
    void getCompilations_withoutPinnedFilter_shouldReturnAllCompilations() {
        // Создаем еще одну подборку
        Compilation anotherCompilation = Compilation.builder()
                .title("Another Compilation")
                .pinned(false)
                .events(Set.of(event2))
                .build();
        compilationRepository.save(anotherCompilation);

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getCompilation_shouldReturnCompilationSuccessfully() {
        CompilationDto result = compilationService.getCompilation(existingCompilation.getId());

        assertEquals(existingCompilation.getId(), result.getId());
        assertEquals("Existing Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(1, result.getEvents().size());
    }

    @Test
    void getCompilation_withNonExistentId_shouldThrowException() {
        assertThrows(CompilationNotFoundException.class, () -> {
            compilationService.getCompilation(999L);
        });
    }
}