package ru.practicum.main.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CommentsRepositoryTest {

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByStateIn_ShouldReturnCommentsWithGivenStates() {
        // Given
        User user = createUser();
        Event event = createEvent(user);

        Comment pendingComment = createComment("Pending comment", user, event, CommentStatus.PENDING);
        Comment approvedComment = createComment("Approved comment", user, event, CommentStatus.APPROVED);
        Comment rejectedComment = createComment("Rejected comment", user, event, CommentStatus.REJECTED);

        Pageable pageable = PageRequest.of(0, 10);
        List<CommentStatus> states = List.of(CommentStatus.PENDING, CommentStatus.APPROVED);

        // When
        Page<Comment> result = commentsRepository.findByStateIn(states, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .anyMatch(c -> c.getState() == CommentStatus.PENDING));
        assertTrue(result.getContent().stream()
                .anyMatch(c -> c.getState() == CommentStatus.APPROVED));
        assertTrue(result.getContent().stream()
                .noneMatch(c -> c.getState() == CommentStatus.REJECTED));
    }

    @Test
    void findByAuthorId_ShouldReturnCommentsByAuthor() {
        // Given
        User author1 = createUser();
        User author2 = createUser();
        Event event = createEvent(author1);

        Comment comment1 = createComment("Comment 1", author1, event, CommentStatus.PENDING);
        Comment comment2 = createComment("Comment 2", author1, event, CommentStatus.APPROVED);
        createComment("Comment 3", author2, event, CommentStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Comment> result = commentsRepository.findByAuthorId(author1.getId(), pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(c -> c.getAuthor().getId().equals(author1.getId())));
    }

    @Test
    void save_ShouldPersistComment() {
        // Given
        User user = createUser();
        Event event = createEvent(user);
        Comment comment = Comment.builder()
                .text("Test comment")
                .author(user)
                .event(event)
                .state(CommentStatus.PENDING)
                .build();

        // When
        Comment saved = commentsRepository.save(comment);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test comment", saved.getText());
        assertEquals(user.getId(), saved.getAuthor().getId());
        assertEquals(event.getId(), saved.getEvent().getId());
        assertEquals(CommentStatus.PENDING, saved.getState());
    }

    @Test
    void findById_ShouldReturnComment() {
        // Given
        User user = createUser();
        Event event = createEvent(user);
        Comment comment = createComment("Test comment", user, event, CommentStatus.PENDING);

        // When
        Comment found = commentsRepository.findById(comment.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(comment.getId(), found.getId());
        assertEquals("Test comment", found.getText());
    }

    // Helper methods
    private User createUser() {
        User user = User.builder()
                .name("Test User")
                .email("test" + System.currentTimeMillis() + "@example.com")
                .build();
        return userRepository.save(user);
    }

    private Event createEvent(User initiator) {
        Category category = Category.builder()
                .name("Test Category")
                .build();
        categoryRepository.save(category);

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
        return eventRepository.save(event);
    }

    private Comment createComment(String text, User author, Event event, CommentStatus state) {
        Comment comment = Comment.builder()
                .text(text)
                .author(author)
                .event(event)
                .state(state)
                .build();
        return commentsRepository.save(comment);
    }
}