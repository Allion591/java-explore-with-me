package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.enums.EventState;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventModelTest {

    @Test
    void event_ShouldCreateWithBuilder() {
        // Given & When
        Event event = Event.builder()
                .id(1L)
                .annotation("Test annotation")
                .description("Test description")
                .eventDate(LocalDateTime.now().plusDays(5))
                .title("Test Event")
                .build();

        // Then
        assertNotNull(event);
        assertEquals(1L, event.getId());
        assertEquals("Test annotation", event.getAnnotation());
        assertEquals("Test description", event.getDescription());
        assertEquals("Test Event", event.getTitle());
        assertNotNull(event.getEventDate());
        assertFalse(event.getPaid()); // значение по умолчанию
        assertEquals(0, event.getParticipantLimit()); // значение по умолчанию
        assertTrue(event.getRequestModeration()); // значение по умолчанию
        assertEquals(EventState.PENDING, event.getState()); // значение по умолчанию
        assertEquals(0L, event.getViews()); // значение по умолчанию
        assertEquals(0L, event.getConfirmedRequests()); // значение по умолчанию
    }

    @Test
    void event_ShouldUseDefaultValues() {
        // Given & When - создание без указания полей со значениями по умолчанию
        Event event = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .build();

        // Then - проверяем значения по умолчанию
        assertNotNull(event);
        assertFalse(event.getPaid());
        assertEquals(0, event.getParticipantLimit());
        assertTrue(event.getRequestModeration());
        assertEquals(EventState.PENDING, event.getState());
        assertEquals(0L, event.getViews());
        assertEquals(0L, event.getConfirmedRequests());
    }

    @Test
    void event_ShouldHaveNoArgsConstructor() {
        // Given & When
        Event event = new Event();
        event.setId(1L);
        event.setAnnotation("Annotation");
        event.setDescription("Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setTitle("Title");
        event.setPaid(true);
        event.setParticipantLimit(100);
        event.setRequestModeration(false);
        event.setState(EventState.PUBLISHED);

        // Then
        assertNotNull(event);
        assertEquals(1L, event.getId());
        assertEquals("Annotation", event.getAnnotation());
        assertEquals("Description", event.getDescription());
        assertEquals("Title", event.getTitle());
        assertTrue(event.getPaid());
        assertEquals(100, event.getParticipantLimit());
        assertFalse(event.getRequestModeration());
        assertEquals(EventState.PUBLISHED, event.getState());
    }

    @Test
    void event_ShouldHaveAllArgsConstructor() {
        // Given & When
        LocalDateTime eventDate = LocalDateTime.now().plusDays(5);
        LocalDateTime createdOn = LocalDateTime.now().minusDays(1);
        LocalDateTime publishedOn = LocalDateTime.now();

        Event event = new Event(
                1L, "Annotation", null, "Description", eventDate,
                null, false, 50, true, "Title", null,
                EventState.PENDING, createdOn, publishedOn, null, 100L, 25L
        );

        // Then
        assertNotNull(event);
        assertEquals(1L, event.getId());
        assertEquals("Annotation", event.getAnnotation());
        assertEquals("Description", event.getDescription());
        assertEquals(eventDate, event.getEventDate());
        assertEquals("Title", event.getTitle());
        assertFalse(event.getPaid());
        assertEquals(50, event.getParticipantLimit());
        assertTrue(event.getRequestModeration());
        assertEquals(EventState.PENDING, event.getState());
        assertEquals(createdOn, event.getCreatedOn());
        assertEquals(publishedOn, event.getPublishedOn());
        assertEquals(100L, event.getViews());
        assertEquals(25L, event.getConfirmedRequests());
    }

    @Test
    void event_ShouldHaveLombokFunctionality() {
        // Given & When
        Event event1 = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .build();

        Event event2 = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .build();

        Event event3 = Event.builder()
                .id(2L)
                .annotation("Different Annotation")
                .description("Different Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .title("Different Title")
                .build();

        // Then
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1.hashCode(), event3.hashCode());

        assertNotNull(event1.toString());
        assertTrue(event1.toString().contains("Title"));
    }

    @Test
    void event_ShouldHandleRelationships() {
        // Given
        User user = User.builder().id(1L).name("User").build();
        Category category = Category.builder().id(1L).name("Category").build();
        Location location = Location.builder().lat(55.7558f).lon(37.6173f).build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .initiator(user)
                .category(category)
                .location(location)
                .requests(Collections.emptyList())
                .build();

        // Then
        assertNotNull(event);
        assertEquals(user, event.getInitiator());
        assertEquals(category, event.getCategory());
        assertEquals(location, event.getLocation());
        assertNotNull(event.getRequests());
        assertTrue(event.getRequests().isEmpty());
    }

    @Test
    void event_ShouldUpdateStateAndDates() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .state(EventState.PENDING)
                .publishedOn(null)
                .build();

        // When
        event.setState(EventState.PUBLISHED);
        LocalDateTime publishedDate = LocalDateTime.now();
        event.setPublishedOn(publishedDate);

        // Then
        assertEquals(EventState.PUBLISHED, event.getState());
        assertEquals(publishedDate, event.getPublishedOn());
    }

    @Test
    void event_ShouldHandleViewsAndConfirmedRequests() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Title")
                .build();

        // When
        event.setViews(150L);
        event.setConfirmedRequests(75L);

        // Then
        assertEquals(150L, event.getViews());
        assertEquals(75L, event.getConfirmedRequests());
    }
}