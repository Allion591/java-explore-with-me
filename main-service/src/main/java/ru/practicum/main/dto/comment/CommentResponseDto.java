package ru.practicum.main.dto.comment;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponseDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String state;
}