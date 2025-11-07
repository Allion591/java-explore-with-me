package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserModelTest {

    @Test
    void user_ShouldCreateWithBuilder() {
        // Given & When
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertNull(user.getEvents());
        assertNull(user.getRequests());
        assertNull(user.getCreatedOn());
    }

    @Test
    void user_ShouldHaveNoArgsConstructor() {
        // Given & When
        User user = new User();
        user.setId(1L);
        user.setName("Jane Smith");
        user.setEmail("jane.smith@example.com");
        user.setEvents(Collections.emptyList());
        user.setRequests(Collections.emptyList());

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Jane Smith", user.getName());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertNotNull(user.getEvents());
        assertTrue(user.getEvents().isEmpty());
        assertNotNull(user.getRequests());
        assertTrue(user.getRequests().isEmpty());
    }

    @Test
    void user_ShouldHaveAllArgsConstructor() {
        // Given & When
        LocalDateTime createdOn = LocalDateTime.now().minusDays(1);
        User user = new User(1L, "Test User", "test@example.com",
                Collections.emptyList(), Collections.emptyList(), createdOn);

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertNotNull(user.getEvents());
        assertTrue(user.getEvents().isEmpty());
        assertNotNull(user.getRequests());
        assertTrue(user.getRequests().isEmpty());
        assertEquals(createdOn, user.getCreatedOn());
    }

    @Test
    void user_ShouldHaveLombokFunctionality() {
        // Given & When
        User user1 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());

        assertNotNull(user1.toString());
        assertTrue(user1.toString().contains("John Doe"));
        assertTrue(user1.toString().contains("john@example.com"));
    }

    @Test
    void user_ShouldHandleRelationships() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        Event event = Event.builder()
                .id(1L)
                .title("Test Event")
                .annotation("Test annotation")
                .description("Test description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(user)
                .build();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .requester(user)
                .build();

        // When
        user.setEvents(Collections.singletonList(event));
        user.setRequests(Collections.singletonList(request));

        // Then
        assertNotNull(user.getEvents());
        assertEquals(1, user.getEvents().size());
        assertEquals(event, user.getEvents().get(0));

        assertNotNull(user.getRequests());
        assertEquals(1, user.getRequests().size());
        assertEquals(request, user.getRequests().get(0));
    }

    @Test
    void user_ShouldUpdateEmail() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("old@example.com")
                .build();

        // When
        user.setEmail("new@example.com");

        // Then
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void user_ShouldHandleCreationTimestamp() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // When
        LocalDateTime createdDate = LocalDateTime.now().minusDays(5);
        user.setCreatedOn(createdDate);

        // Then
        assertEquals(createdDate, user.getCreatedOn());
    }

    @Test
    void user_ShouldHandleNullCollections() {
        // Given & When
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .events(null)
                .requests(null)
                .build();

        // Then
        assertNotNull(user);
        assertNull(user.getEvents());
        assertNull(user.getRequests());
    }
}