package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toCommentResponseDto_WhenNullComment_ThenReturnNull() {
        // When
        CommentResponseDto dto = commentMapper.toCommentResponseDto(null);

        // Then
        assertNull(dto, "DTO должен быть null при null входном объекте");
    }

    @Test
    void toCommentResponseDto_WhenCommentWithNullAuthor_ThenMapWithNullAuthorId() {
        // Given
        Event event = Event.builder()
                .id(100L)
                .title("Test Event")
                .build();

        Comment comment = Comment.builder()
                .id(10L)
                .text("Comment without author")
                .author(null)
                .event(event)
                .created(LocalDateTime.now())
                .state(CommentStatus.PENDING)
                .build();

        // When
        CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);

        // Then
        assertNotNull(dto, "DTO не должен быть null");
        assertNull(dto.getAuthorId(), "AuthorId должен быть null при отсутствии author");
        assertEquals(comment.getEvent().getId(), dto.getEventId(), "EventId должен быть корректно установлен");
    }

    @Test
    void toCommentResponseDto_WhenCommentWithNullEvent_ThenMapWithNullEventId() {
        // Given
        User author = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        Comment comment = Comment.builder()
                .id(10L)
                .text("Comment without event")
                .author(author)
                .event(null)
                .created(LocalDateTime.now())
                .state(CommentStatus.REJECTED)
                .build();

        // When
        CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);

        // Then
        assertNotNull(dto, "DTO не должен быть null");
        assertNull(dto.getEventId(), "EventId должен быть null при отсутствии event");
        assertEquals(comment.getAuthor().getId(), dto.getAuthorId(), "AuthorId должен быть корректно установлен");
    }

    @Test
    void toComment_WhenNullCommentDto_ThenReturnNull() {
        // When
        Comment comment = commentMapper.toComment(null);

        // Then
        assertNull(comment, "Comment должен быть null при null входном объекте");
    }

    @Test
    void toComment_WhenCommentDtoWithEmptyText_ThenMapWithEmptyText() {
        // Given
        CommentDto commentDto = CommentDto.builder()
                .text("")
                .build();

        // When
        Comment comment = commentMapper.toComment(commentDto);

        // Then
        assertNotNull(comment, "Comment не должен быть null");
        assertEquals("", comment.getText(), "Text должен быть пустой строкой");
    }

    @Test
    void toComment_WhenCommentDtoWithNullText_ThenMapWithNullText() {
        // Given
        CommentDto commentDto = CommentDto.builder()
                .text(null)
                .build();

        // When
        Comment comment = commentMapper.toComment(commentDto);

        // Then
        assertNotNull(comment, "Comment не должен быть null");
        assertNull(comment.getText(), "Text должен быть null");
    }

    @Test
    void toComment_WhenCommentDtoWithMaxLengthText_ThenMapCorrectly() {
        // Given
        String maxLengthText = "A".repeat(255);
        CommentDto commentDto = CommentDto.builder()
                .text(maxLengthText)
                .build();

        // When
        Comment comment = commentMapper.toComment(commentDto);

        // Then
        assertNotNull(comment, "Comment не должен быть null");
        assertEquals(maxLengthText, comment.getText(), "Text максимальной длины должен корректно маппиться");
    }

    @Test
    void toComment_VerifyIdIsIgnored() {
        // Given
        CommentDto commentDto = CommentDto.builder()
                .text("Test comment")
                .build();

        // When
        Comment comment = commentMapper.toComment(commentDto);

        // Then
        assertNotNull(comment, "Comment не должен быть null");
        assertNull(comment.getId(), "ID должен быть null, так как он игнорируется в маппинге");
    }

    @Test
    void roundTrip_CommentToDtoAndBack_ShouldNotBePossible() {
        // Given
        User author = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        Event event = Event.builder()
                .id(100L)
                .title("Test Event")
                .build();

        Comment originalComment = Comment.builder()
                .id(10L)
                .text("Original comment")
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .state(CommentStatus.APPROVED)
                .build();

        // When
        CommentResponseDto dto = commentMapper.toCommentResponseDto(originalComment);

        // Then - мы не можем сделать обратный маппинг из CommentResponseDto в Comment,
        // так как нет соответствующего метода в маппере
        // Это нормально, так как маппер не обязательно должен быть двусторонним
        assertNotNull(dto, "DTO не должен быть null");
    }

    @Test
    void multipleMappings_ShouldWorkConsistently() {
        // Given
        User author = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        Event event = Event.builder()
                .id(100L)
                .title("Test Event")
                .build();

        Comment comment = Comment.builder()
                .id(10L)
                .text("Consistency test comment")
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .state(CommentStatus.PENDING)
                .build();

        CommentDto commentDto = CommentDto.builder()
                .text("Consistency test DTO")
                .build();

        // When & Then - multiple executions should produce same results
        for (int i = 0; i < 5; i++) {
            CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);
            Comment mappedComment = commentMapper.toComment(commentDto);

            assertNotNull(dto, "DTO не должен быть null при многократном вызове");
            assertNotNull(mappedComment, "Comment не должен быть null при многократном вызове");
            assertEquals(comment.getText(), dto.getText(), "Text должен сохраняться при многократном маппинге");
            assertEquals(commentDto.getText(), mappedComment.getText(), "Text должен сохраняться при многократном маппинге");
        }
    }

    @Test
    void mapping_WithSpecialCharactersInText_ShouldWorkCorrectly() {
        // Given
        String textWithSpecialChars = "Comment with special chars: ñ, é, 中文, 🎉";
        CommentDto commentDto = CommentDto.builder()
                .text(textWithSpecialChars)
                .build();

        User author = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(100L)
                .title("Test Event")
                .build();

        Comment comment = Comment.builder()
                .id(10L)
                .text(textWithSpecialChars)
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .state(CommentStatus.APPROVED)
                .build();

        // When
        Comment mappedComment = commentMapper.toComment(commentDto);
        CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);

        // Then
        assertNotNull(mappedComment, "Mapped comment не должен быть null");
        assertEquals(textWithSpecialChars, mappedComment.getText(), "Специальные символы должны сохраняться в Comment");

        assertNotNull(dto, "DTO не должен быть null");
        assertEquals(textWithSpecialChars, dto.getText(), "Специальные символы должны сохраняться в DTO");
    }
}