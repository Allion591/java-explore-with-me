package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.exception.conflict.ConflictException;
import ru.practicum.main.exception.notFound.NotFoundException;
import ru.practicum.main.exception.validation.ValidationException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentsRepository;
import ru.practicum.main.service.implementations.CommentServiceImpl;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private CommentsRepository repository;

    @Mock
    private CommentMapper mapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void updateCommentByAdmin_WhenApprove_ShouldUpdateState() {
        // Given
        Long commentId = 1L;
        UpdateCommentAdminRequest request = new UpdateCommentAdminRequest(CommentStatus.APPROVED);
        Comment comment = createComment(commentId, CommentStatus.PENDING);
        CommentResponseDto responseDto = createCommentResponseDto(commentId, "APPROVED");

        when(repository.findById(commentId)).thenReturn(Optional.of(comment));
        when(repository.save(any(Comment.class))).thenReturn(comment);
        when(mapper.toCommentResponseDto(comment)).thenReturn(responseDto);

        // When
        CommentResponseDto result = commentService.updateCommentByAdmin(commentId, request);

        // Then
        assertNotNull(result);
        assertEquals("APPROVED", result.getState());
        verify(repository).save(comment);
        assertEquals(CommentStatus.APPROVED, comment.getState());
    }

    @Test
    void getCommentsForModeration_ShouldReturnPendingAndEditedComments() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").ascending());

        Comment comment1 = createComment(1L, CommentStatus.PENDING);
        Comment comment2 = createComment(2L, CommentStatus.EDITED);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment1, comment2));

        when(repository.findByStateIn(any(), eq(pageable))).thenReturn(commentPage);
        when(mapper.toCommentResponseDto(any(Comment.class)))
                .thenReturn(createCommentResponseDto(1L, "PENDING"))
                .thenReturn(createCommentResponseDto(2L, "EDITED"));

        // When
        List<CommentResponseDto> result = commentService.getCommentsForModeration(page, size);

        // Then
        assertEquals(2, result.size());
        verify(repository).findByStateIn(List.of(CommentStatus.PENDING, CommentStatus.EDITED), pageable);
    }

    @Test
    void saveComment_WhenValid_ShouldSaveComment() {
        // Given
        Long userId = 1L;
        Long eventId = 1L;
        CommentDto commentDto = new CommentDto("Test comment");

        User user = new User();
        user.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);

        Comment comment = new Comment();
        comment.setText("Test comment");

        Comment savedComment = createComment(1L, CommentStatus.PENDING);
        CommentResponseDto responseDto = createCommentResponseDto(1L, "PENDING");

        when(userService.getUserById(userId)).thenReturn(user);
        when(eventService.getEventById(eventId)).thenReturn(event);
        when(mapper.toComment(commentDto)).thenReturn(comment);
        when(repository.save(any(Comment.class))).thenReturn(savedComment);
        when(mapper.toCommentResponseDto(savedComment)).thenReturn(responseDto);

        // When
        CommentResponseDto result = commentService.saveComment(userId, eventId, commentDto);

        // Then
        assertNotNull(result);
        verify(repository).save(any(Comment.class));
    }

    @Test
    void saveComment_WhenEventNotPublished_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long eventId = 1L;
        CommentDto commentDto = new CommentDto("Test comment");

        Event event = new Event();
        event.setState(EventState.PENDING);

        when(eventService.getEventById(eventId)).thenReturn(event);

        // When & Then
        assertThrows(ConflictException.class, () ->
                commentService.saveComment(userId, eventId, commentDto));
    }

    @Test
    void getCommentById_WhenUserIsAuthor_ShouldReturnComment() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;
        Comment comment = createComment(commentId, CommentStatus.PENDING);
        comment.getAuthor().setId(userId);

        CommentResponseDto responseDto = createCommentResponseDto(commentId, "PENDING");

        doNothing().when(userService).checkUserExists(userId);
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));
        when(mapper.toCommentResponseDto(comment)).thenReturn(responseDto);

        // When
        CommentResponseDto result = commentService.getCommentById(userId, commentId);

        // Then
        assertNotNull(result);
        assertEquals(commentId, result.getId());
    }

    @Test
    void getCommentById_WhenUserNotAuthor_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;
        Comment comment = createComment(commentId, CommentStatus.PENDING);
        comment.getAuthor().setId(2L); // Different user

        doNothing().when(userService).checkUserExists(userId);
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThrows(ValidationException.class, () ->
                commentService.getCommentById(userId, commentId));
    }

    @Test
    void getUserComments_ShouldReturnUserComments() {
        // Given
        Long userId = 1L;
        int from = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());

        Comment comment = createComment(1L, CommentStatus.PENDING);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        doNothing().when(userService).checkUserExists(userId);
        when(repository.findByAuthorId(userId, pageable)).thenReturn(commentPage);
        when(mapper.toCommentResponseDto(any(Comment.class)))
                .thenReturn(createCommentResponseDto(1L, "PENDING"));

        // When
        List<CommentResponseDto> result = commentService.getUserComments(userId, from, size);

        // Then
        assertEquals(1, result.size());
        verify(repository).findByAuthorId(userId, pageable);
    }

    @Test
    void updateCommentByUser_WhenValid_ShouldUpdateComment() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;
        CommentDto commentDto = new CommentDto("Updated text");

        Comment comment = createComment(commentId, CommentStatus.PENDING);
        comment.getAuthor().setId(userId);

        CommentResponseDto responseDto = createCommentResponseDto(commentId, "EDITED");

        doNothing().when(userService).checkUserExists(userId);
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));
        when(repository.save(any(Comment.class))).thenReturn(comment);
        when(mapper.toCommentResponseDto(comment)).thenReturn(responseDto);

        // When
        CommentResponseDto result = commentService.updateCommentByUser(userId, commentId, commentDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated text", comment.getText());
        assertEquals(CommentStatus.EDITED, comment.getState());
        verify(repository).save(comment);
    }

    @Test
    void deleteComment_WhenValid_ShouldMarkAsDeleted() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;
        Comment comment = createComment(commentId, CommentStatus.PENDING);
        comment.getAuthor().setId(userId);

        doNothing().when(userService).checkUserExists(userId);
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));
        when(repository.save(any(Comment.class))).thenReturn(comment);

        // When
        commentService.deleteComment(userId, commentId);

        // Then
        assertEquals(CommentStatus.DELETED, comment.getState());
        verify(repository).save(comment);
    }

    @Test
    void getAllByEventId_ShouldReturnApprovedComments() {
        // Given
        Long eventId = 1L;
        int from = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());

        Comment comment = createComment(1L, CommentStatus.APPROVED);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        doNothing().when(eventService).existsById(eventId);
        when(repository.findByEventIdAndState(eventId, CommentStatus.APPROVED, pageable))
                .thenReturn(commentPage);
        when(mapper.toCommentResponseDto(any(Comment.class)))
                .thenReturn(createCommentResponseDto(1L, "APPROVED"));

        // When
        List<CommentResponseDto> result = commentService.getAllByEventId(eventId, from, size);

        // Then
        assertEquals(1, result.size());
        verify(repository).findByEventIdAndState(eventId, CommentStatus.APPROVED, pageable);
    }

    @Test
    void updateCommentByAdmin_WhenCommentNotFound_ShouldThrowException() {
        // Given
        Long commentId = 1L;
        UpdateCommentAdminRequest request = new UpdateCommentAdminRequest(CommentStatus.APPROVED);

        when(repository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                commentService.updateCommentByAdmin(commentId, request));
    }

    // Helper methods
    private Comment createComment(Long id, CommentStatus status) {
        User author = new User();
        author.setId(1L);

        Event event = new Event();
        event.setId(1L);

        return Comment.builder()
                .id(id)
                .text("Test comment")
                .author(author)
                .event(event)
                .state(status)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();
    }

    private CommentResponseDto createCommentResponseDto(Long id, String state) {
        return CommentResponseDto.builder()
                .id(id)
                .text("Test comment")
                .eventId(1L)
                .authorId(1L)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .state(state)
                .build();
    }
}