package ru.practicum.main.controller.publics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.service.interfaces.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService service;

    // Получение всех комментариев события для неавторизованных пользователей
    @GetMapping("/{eventId}")
    public List<CommentResponseDto> getAllCommentsByEventId(@PathVariable @Min(1) Long eventId,
                                                            @RequestParam(defaultValue = "0") @Min(0) int from,
                                                            @RequestParam(defaultValue = "10") @Max(100) int size) {
        log.info("Public контроллер: Принял запрос на вывод всех комментариев события id: {}", eventId);
        return service.getAllByEventId(eventId, from, size);
    }
}