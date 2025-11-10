package ru.practicum.main.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.main.enums.ParticipationRequestStatus;
import ru.practicum.main.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ParticipationRequestRepositoryIntegrationTest {

    @Autowired
    private ParticipationRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user1, user2;
    private Event event1, event2;
    private ParticipationRequest request1, request2, request3;

    @BeforeEach
    void setUp() {
        // Создаем пользователей
        user1 = User.builder()
                .name("User 1")
                .email("user1@email.com")
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .name("User 2")
                .email("user2@email.com")
                .build();
        user2 = userRepository.save(user2);

        // Создаем категорию
        Category category = Category.builder()
                .name("Test Category")
                .build();
        category = categoryRepository.save(category);

        // Создаем локацию
        Location location = new Location(55.7558f, 37.6173f);

        // Создаем события
        event1 = Event.builder()
                .annotation("Test annotation 1")
                .category(category)
                .description("Test description 1")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Test Event 1")
                .initiator(user1)
                .state(ru.practicum.main.enums.EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .build();
        event1 = eventRepository.save(event1);

        event2 = Event.builder()
                .annotation("Test annotation 2")
                .category(category)
                .description("Test description 2")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(location)
                .paid(false)
                .participantLimit(5)
                .requestModeration(true)
                .title("Test Event 2")
                .initiator(user1)
                .state(ru.practicum.main.enums.EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .build();
        event2 = eventRepository.save(event2);

        // Создаем заявки на участие
        request1 = ParticipationRequest.builder()
                .event(event1)
                .requester(user2)
                .status(ParticipationRequestStatus.PENDING)
                .build();
        request1 = requestRepository.save(request1);

        request2 = ParticipationRequest.builder()
                .event(event1)
                .requester(user1)
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();
        request2 = requestRepository.save(request2);

        request3 = ParticipationRequest.builder()
                .event(event2)
                .requester(user2)
                .status(ParticipationRequestStatus.REJECTED)
                .build();
        request3 = requestRepository.save(request3);
    }

    @Test
    void findByRequesterId_shouldReturnUserRequests() {
        List<ParticipationRequest> result = requestRepository.findByRequesterId(user2.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(request -> request.getRequester().getId().equals(user2.getId())));
    }

    @Test
    void findByEventId_shouldReturnEventRequests() {
        List<ParticipationRequest> result = requestRepository.findByEventId(event1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(request -> request.getEvent().getId().equals(event1.getId())));
    }

    @Test
    void findByEventIdAndStatus_shouldReturnFilteredRequests() {
        List<ParticipationRequest> result = requestRepository.findByEventIdAndStatus(
                event1.getId(), ParticipationRequestStatus.PENDING
        );

        assertEquals(1, result.size());
        assertEquals(ParticipationRequestStatus.PENDING, result.get(0).getStatus());
        assertEquals(event1.getId(), result.get(0).getEvent().getId());
    }

    @Test
    void findByRequesterIdAndEventId_whenExists_shouldReturnRequest() {
        Optional<ParticipationRequest> result = requestRepository.findByRequesterIdAndEventId(
                user2.getId(), event1.getId()
        );

        assertTrue(result.isPresent());
        assertEquals(user2.getId(), result.get().getRequester().getId());
        assertEquals(event1.getId(), result.get().getEvent().getId());
    }

    @Test
    void findByRequesterIdAndEventId_whenNotExists_shouldReturnEmpty() {
        Optional<ParticipationRequest> result = requestRepository.findByRequesterIdAndEventId(
                user1.getId(), event2.getId()
        );

        assertFalse(result.isPresent());
    }

    @Test
    void countConfirmedRequestsByEventId_shouldReturnConfirmedCount() {
        Long count = requestRepository.countConfirmedRequestsByEventId(event1.getId());

        assertEquals(1L, count); // Только request2 имеет статус CONFIRMED для event1
    }

    @Test
    void countConfirmedRequestsByEventId_whenNoConfirmed_shouldReturnZero() {
        Long count = requestRepository.countConfirmedRequestsByEventId(event2.getId());

        assertEquals(0L, count); // request3 имеет статус REJECTED
    }

    @Test
    void countConfirmedRequestsByEventIds_shouldReturnCountsForMultipleEvents() {
        List<Object[]> result = requestRepository.countConfirmedRequestsByEventIds(
                List.of(event1.getId(), event2.getId())
        );

        assertEquals(1, result.size()); // Только event1 имеет подтвержденные заявки
        assertEquals(event1.getId(), result.get(0)[0]);
        assertEquals(1L, result.get(0)[1]);
    }

    @Test
    void findByEventIdAndIdIn_shouldReturnMatchingRequests() {
        List<ParticipationRequest> result = requestRepository.findByEventIdAndIdIn(
                event1.getId(), List.of(request1.getId(), request2.getId())
        );

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(request -> request.getEvent().getId().equals(event1.getId())));
    }

    @Test
    void findByEventIdAndIdIn_withPartialMatch_shouldReturnOnlyExisting() {
        List<ParticipationRequest> result = requestRepository.findByEventIdAndIdIn(
                event1.getId(), List.of(request1.getId(), 999L) // 999L не существует
        );

        assertEquals(1, result.size());
        assertEquals(request1.getId(), result.get(0).getId());
    }

    @Test
    void existsByRequesterIdAndEventId_whenExists_shouldReturnTrue() {
        boolean exists = requestRepository.existsByRequesterIdAndEventId(
                user2.getId(), event1.getId()
        );

        assertTrue(exists);
    }

    @Test
    void existsByRequesterIdAndEventId_whenNotExists_shouldReturnFalse() {
        boolean exists = requestRepository.existsByRequesterIdAndEventId(
                user1.getId(), event2.getId()
        );

        assertFalse(exists);
    }

    @Test
    void findByIdIn_shouldReturnRequestsByIds() {
        List<ParticipationRequest> result = requestRepository.findByIdIn(
                List.of(request1.getId(), request3.getId())
        );

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(request -> request.getId().equals(request1.getId())));
        assertTrue(result.stream()
                .anyMatch(request -> request.getId().equals(request3.getId())));
    }

    @Test
    void findByIdIn_withNonExistingIds_shouldReturnOnlyExisting() {
        List<ParticipationRequest> result = requestRepository.findByIdIn(
                List.of(request1.getId(), 999L, 1000L)
        );

        assertEquals(1, result.size());
        assertEquals(request1.getId(), result.get(0).getId());
    }
}