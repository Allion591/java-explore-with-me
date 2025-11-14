package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.exception.conflict.ConflictException;
import ru.practicum.main.exception.notFound.NotFoundException;
import ru.practicum.main.exception.validation.ValidationException;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentsRepository;
import ru.practicum.main.service.implementations.CommentServiceImpl;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentServiceImpl commentService;

    @MockBean
    private UserService userService;

    @MockBean
    private EventService eventService;

    @MockBean
    private CommentsRepository commentsRepository;

    @Test
    void updateCommentByAdmin_WhenValidRequest_ShouldUpdateComment() {
        // Given
        Long commentId = 1L;
        UpdateCommentAdminRequest request = new UpdateCommentAdminRequest("APPROVE");

        Comment comment = Comment.builder()
                .id(commentId)
                .text("Test comment")
                .state(CommentStatus.PENDING)
                .build();

        when(commentsRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentsRepository.save(any(Comment.class))).thenReturn(comment);

        // When
        CommentResponseDto result = commentService.updateCommentByAdmin(commentId, request);

        // Then
        assertNotNull(result);
        verify(commentsRepository).save(comment);
        assertEquals(CommentStatus.APPROVED, comment.getState());
    }

    @Test
    void saveComment_WhenEventPublished_ShouldCreateComment() {
        // Given
        Long userId = 1L;
        Long eventId = 1L;
        CommentDto commentDto = new CommentDto("New comment");

        User user = User.builder().id(userId).build();
        Event event = Event.builder().id(eventId).state(EventState.PUBLISHED).build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("New comment")
                .author(user)
                .event(event)
                .state(CommentStatus.PENDING)
                .build();

        when(userService.getUserById(userId)).thenReturn(user);
        when(eventService.getEventById(eventId)).thenReturn(event);
        when(commentsRepository.save(any(Comment.class))).thenReturn(comment);

        // When
        CommentResponseDto result = commentService.saveComment(userId, eventId, commentDto);

        // Then
        assertNotNull(result);
        verify(commentsRepository).save(any(Comment.class));
    }

    @Test
    void saveComment_WhenEventNotPublished_ShouldThrowConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 1L;
        CommentDto commentDto = new CommentDto("New comment");

        Event event = Event.builder().id(eventId).state(EventState.PENDING).build();

        when(eventService.getEventById(eventId)).thenReturn(event);

        // When & Then
        assertThrows(ConflictException.class, () ->
                commentService.saveComment(userId, eventId, commentDto));
    }

    @Test
    void getCommentById_WhenUserNotAuthor_ShouldThrowValidationException() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;

        User author = User.builder().id(2L).build(); // Different user
        Comment comment = Comment.builder()
                .id(commentId)
                .author(author)
                .build();

        doNothing().when(userService).checkUserExists(userId);
        when(commentsRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThrows(ValidationException.class, () ->
                commentService.getCommentById(userId, commentId));
    }

    @Test
    void updateCommentByUser_WhenUserNotAuthor_ShouldThrowConflictException() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;
        CommentDto commentDto = new CommentDto("Updated text");

        User author = User.builder().id(2L).build(); // Different user
        Comment comment = Comment.builder()
                .id(commentId)
                .author(author)
                .build();

        doNothing().when(userService).checkUserExists(userId);
        when(commentsRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThrows(ConflictException.class, () ->
                commentService.updateCommentByUser(userId, commentId, commentDto));
    }

    @Test
    void deleteComment_WhenAlreadyDeleted_ShouldThrowConflictException() {
        // Given
        Long userId = 1L;
        Long commentId = 1L;

        User author = User.builder().id(userId).build();
        Comment comment = Comment.builder()
                .id(commentId)
                .author(author)
                .state(CommentStatus.DELETED)
                .build();

        doNothing().when(userService).checkUserExists(userId);
        when(commentsRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThrows(ConflictException.class, () ->
                commentService.deleteComment(userId, commentId));
    }

    @Test
    void findByIdCheck_WhenCommentNotFound_ShouldThrowNotFoundException() {
        // Given
        Long commentId = 999L;
        when(commentsRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                commentService.updateCommentByAdmin(commentId, new UpdateCommentAdminRequest("APPROVE")));
    }
}