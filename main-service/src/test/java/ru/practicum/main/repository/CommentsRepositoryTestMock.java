package ru.practicum.main.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.model.Comment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentsRepositoryTestMock {

    @Mock
    private CommentsRepository commentsRepository;

    @Test
    void findByStateIn_ShouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentStatus> states = List.of(CommentStatus.PENDING, CommentStatus.APPROVED);
        Page<Comment> expectedPage = new PageImpl<>(List.of(new Comment(), new Comment()));

        when(commentsRepository.findByStateIn(eq(states), eq(pageable)))
                .thenReturn(expectedPage);

        // When
        Page<Comment> result = commentsRepository.findByStateIn(states, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(commentsRepository, times(1)).findByStateIn(states, pageable);
    }

    @Test
    void findByAuthorId_ShouldReturnPage() {
        // Given
        Long authorId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> expectedPage = new PageImpl<>(List.of(new Comment()));

        when(commentsRepository.findByAuthorId(eq(authorId), eq(pageable)))
                .thenReturn(expectedPage);

        // When
        Page<Comment> result = commentsRepository.findByAuthorId(authorId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(commentsRepository, times(1)).findByAuthorId(authorId, pageable);
    }

    @Test
    void findByEventIdAndState_ShouldReturnPage() {
        // Given
        Long eventId = 1L;
        CommentStatus state = CommentStatus.APPROVED;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> expectedPage = new PageImpl<>(List.of(new Comment(), new Comment()));

        when(commentsRepository.findByEventIdAndState(eq(eventId), eq(state), eq(pageable)))
                .thenReturn(expectedPage);

        // When
        Page<Comment> result = commentsRepository.findByEventIdAndState(eventId, state, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(commentsRepository, times(1)).findByEventIdAndState(eventId, state, pageable);
    }

    @Test
    void save_ShouldReturnSavedComment() {
        // Given
        Comment comment = new Comment();
        Comment savedComment = new Comment();
        savedComment.setId(1L);

        when(commentsRepository.save(any(Comment.class))).thenReturn(savedComment);

        // When
        Comment result = commentsRepository.save(comment);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(commentsRepository, times(1)).save(comment);
    }

    @Test
    void findById_ShouldReturnComment() {
        // Given
        Long commentId = 1L;
        Comment comment = new Comment();
        comment.setId(commentId);

        when(commentsRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        Optional<Comment> result = commentsRepository.findById(commentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(commentId, result.get().getId());
        verify(commentsRepository, times(1)).findById(commentId);
    }

    @Test
    void findById_WhenNotFound_ShouldReturnEmpty() {
        // Given
        Long commentId = 999L;

        when(commentsRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        Optional<Comment> result = commentsRepository.findById(commentId);

        // Then
        assertFalse(result.isPresent());
        verify(commentsRepository, times(1)).findById(commentId);
    }
}