package ru.practicum.main.service.interfaces;

import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;

import java.util.List;

public interface CommentService {

    // Сохранение коммениария только авторизованным пользователем
    CommentResponseDto saveComment(Long userId, Long eventId, CommentDto commentDto);

    // Обновление комментария только авторизованным пользователем
    CommentResponseDto updateCommentByUser(Long userId, Long commentId, CommentDto commentDto);

    // Админский метод только для модерации(обновление статуса)
    CommentResponseDto updateCommentByAdmin(Long commentId, UpdateCommentAdminRequest request);

    // Получение комментария создателем
    CommentResponseDto getCommentById(Long userId, Long commentId);

    // Получение всех комментариев события для неавторизованных пользователей
    List<CommentResponseDto> getAllByEventId(Long eventId, int from, int size);

    // Получение всех комментариев пользователя
    List<CommentResponseDto> getUserComments(Long userId, int from, int size);

    // Получение комментариев ожидающих модерацию(для админа)
    List<CommentResponseDto> getCommentsForModeration(int page, int size);

    // Удаление комментария пользователем
    void deleteComment(Long userId, Long commentId);
}