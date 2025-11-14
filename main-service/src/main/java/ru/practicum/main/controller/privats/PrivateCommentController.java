package ru.practicum.main.controller.privats;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.service.interfaces.CommentService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {
    private final CommentService service;

    // Создание нового комментария
    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto saveComment(
            @PathVariable @Min(1) Long eventId,
            @PathVariable @Min(1) Long userId,
            @RequestBody @Valid CommentDto commentDto) {
        log.info("Private контроллер: Принял запрос на сохранение комментария от пользователя id: {}", userId);

        return service.saveComment(userId, eventId, commentDto);
    }

    // Получение комментария по Id
    @GetMapping("/{commentId}")
    public CommentResponseDto getUserComment(@PathVariable @Min(1) Long userId,
                                             @PathVariable @Min(1) Long commentId) {
        log.info("Private контроллер: Принял запрос на вывод комментария с id: {}, пользователем с id: {}",
                commentId, userId);
        return service.getCommentById(userId, commentId);
    }

    // Получение всех комментариев пользователя
    @GetMapping
    public List<CommentResponseDto> getUserComments(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size) {
        log.info("Private контроллер: Принял запрос на вывод всех комментариев пользователя с id: {}", userId);

        return service.getUserComments(userId, from, size);
    }

    // Обновление комментария только авторизованным пользователем
    @PatchMapping("/{commentId}")
    public CommentResponseDto updateComment(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long commentId,
            @RequestBody @Valid CommentDto request) {
        log.info("Private контроллер: Принял запрос на обновление комментария id: {}, пользователя с id: {}",
                commentId, userId);
        return service.updateCommentByUser(userId, commentId, request);
    }

    // Удаление комментария пользователем (Мягкое удаление, меняем только статус)
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long commentId) {
        log.info("Private контроллер: Принял запрос на удаление комментария id: {}, пользователя с id: {}",
                commentId, userId);
        service.deleteComment(userId, commentId);
    }
}