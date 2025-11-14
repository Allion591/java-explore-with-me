package ru.practicum.main.controller.admins;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.main.service.interfaces.CommentService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService service;

    @PatchMapping("/{commentId}")
    public CommentResponseDto updateStatusComment(@PathVariable @Min(1) Long commentId,
                                                   @Valid @RequestBody UpdateCommentAdminRequest request) {
        log.info("Admin контроллер: Принят запрос на обновление статуса комментария id: {}, status: {}",
                commentId, request.getStateAction());

        return service.updateCommentByAdmin(commentId, request);
    }

    @GetMapping
    public List<CommentResponseDto> getAllPendingComments(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "10")
                                                          @Min(1) @Max(100) int size) {
        log.info("Admin контроллер: Получение всех комментариев ожидающих модерацию");

        return service.getCommentsForModeration(page, size);
    }
}