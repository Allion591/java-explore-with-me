package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.repository.CommentsRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CommentTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentsRepository commentsRepository;

    @Test
    void whenSaveCommentWithValidData_thenSuccess() {
        // Given
        User author = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        entityManager.persistAndFlush(author);

        Category category = Category.builder()
                .name("Test Category")
                .build();
        entityManager.persistAndFlush(category);

        Event event = Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .category(category)
                .initiator(author)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .participantLimit(0)
                .requestModeration(false)
                .paid(false)
                .location(Location.builder().lat(55.7558f).lon(37.6173f).build())
                .build();
        entityManager.persistAndFlush(event);

        Comment comment = Comment.builder()
                .text("This is a test comment")
                .event(event)
                .author(author)
                .state(CommentStatus.PENDING)
                .created(LocalDateTime.now().minusHours(1))
                .updated(LocalDateTime.now())
                .build();

        // When
        Comment savedComment = commentsRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        // Then
        Comment foundComment = entityManager.find(Comment.class, savedComment.getId());
        assertNotNull(foundComment);
        assertEquals("This is a test comment", foundComment.getText());
        assertEquals(event.getId(), foundComment.getEvent().getId());
        assertEquals(author.getId(), foundComment.getAuthor().getId());
        assertEquals(CommentStatus.PENDING, foundComment.getState());
        assertNotNull(foundComment.getCreated());
        assertNotNull(foundComment.getUpdated());
    }

    @Test
    void whenSaveCommentWithoutText_thenThrowException() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        Comment comment = Comment.builder()
                .text(null) // NULL text
                .event(event)
                .author(author)
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentsRepository.save(comment);
            entityManager.flush();
        });
    }

    @Test
    void whenSaveCommentWithTextExceeding255_thenThrowException() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        String longText = "A".repeat(256); // 256 characters
        Comment comment = Comment.builder()
                .text(longText)
                .event(event)
                .author(author)
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentsRepository.save(comment);
            entityManager.flush();
        });
    }

    @Test
    void whenSaveCommentWith255CharacterText_thenSuccess() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        String maxLengthText = "A".repeat(255); // Exactly 255 characters
        Comment comment = Comment.builder()
                .text(maxLengthText)
                .event(event)
                .author(author)
                .build();

        // When
        Comment savedComment = commentsRepository.save(comment);
        entityManager.flush();

        // Then
        assertNotNull(savedComment.getId());
        assertEquals(maxLengthText, savedComment.getText());
    }

    @Test
    void whenSaveCommentWithoutEvent_thenThrowException() {
        // Given
        User author = createTestUser();

        Comment comment = Comment.builder()
                .text("Test comment without event")
                .event(null) // NULL event
                .author(author)
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentsRepository.save(comment);
            entityManager.flush();
        });
    }

    @Test
    void whenSaveCommentWithoutAuthor_thenThrowException() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        Comment comment = Comment.builder()
                .text("Test comment without author")
                .event(event)
                .author(null) // NULL author
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            commentsRepository.save(comment);
            entityManager.flush();
        });
    }

    @Test
    void whenSaveCommentWithoutCreatedDate_thenAutoGenerateCreatedDate() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        Comment comment = Comment.builder()
                .text("Test comment without created date")
                .event(event)
                .author(author)
                .created(null) // NULL created - should auto-generate
                .build();

        // When
        Comment savedComment = commentsRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        // Then
        Comment foundComment = entityManager.find(Comment.class, savedComment.getId());
        assertNotNull(foundComment.getCreated(), "Created date должна генерироваться автоматически");
        assertTrue(foundComment.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(foundComment.getCreated().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void commentStateEnumMapping_ShouldWorkCorrectly() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        CommentStatus[] states = {CommentStatus.PENDING, CommentStatus.APPROVED, CommentStatus.REJECTED};

        for (CommentStatus state : states) {
            Comment comment = Comment.builder()
                    .text("Comment with state: " + state)
                    .event(event)
                    .author(author)
                    .state(state)
                    .build();

            // When
            Comment savedComment = commentsRepository.save(comment);
            entityManager.flush();
            entityManager.clear();

            // Then
            Comment foundComment = entityManager.find(Comment.class, savedComment.getId());
            assertEquals(state, foundComment.getState(),
                    "Enum состояние должно корректно сохраняться и извлекаться для: " + state);
        }
    }

    @Test
    void commentLazyLoading_ShouldWorkForRelations() {
        // Given
        User author = createTestUser();
        Event event = createTestEvent(author);

        Comment comment = Comment.builder()
                .text("Test lazy loading")
                .event(event)
                .author(author)
                .build();

        Comment savedComment = commentsRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        // When
        Comment foundComment = commentsRepository.findById(savedComment.getId()).orElseThrow();

        // Then - проверяем, что отношения загружаются лениво
        // Это можно проверить через Hibernate proxies
        assertNotNull(foundComment.getEvent());
        assertNotNull(foundComment.getAuthor());

        // Проверяем, что это прокси-объекты (ленивая загрузка)
        // Это зависит от реализации Hibernate
        String eventClassName = foundComment.getEvent().getClass().getName();
        String authorClassName = foundComment.getAuthor().getClass().getName();

        assertTrue(eventClassName.contains("HibernateProxy") ||
                        eventClassName.contains("$HibernateProxy$"),
                "Event должен загружаться лениво");
        assertTrue(authorClassName.contains("HibernateProxy") ||
                        authorClassName.contains("$HibernateProxy$"),
                "Author должен загружаться лениво");
    }

    // Helper methods
    private User createTestUser() {
        User user = User.builder()
                .name("Test User")
                .email(System.currentTimeMillis() + "@example.com") // Unique email
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Event createTestEvent(User initiator) {
        Category category = Category.builder()
                .name("Test Category " + System.currentTimeMillis())
                .build();
        entityManager.persistAndFlush(category);

        Event event = Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .category(category)
                .initiator(initiator)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .participantLimit(0)
                .requestModeration(false)
                .paid(false)
                .location(Location.builder().lat(55.7558f).lon(37.6173f).build())
                .build();
        return entityManager.persistAndFlush(event);
    }
}