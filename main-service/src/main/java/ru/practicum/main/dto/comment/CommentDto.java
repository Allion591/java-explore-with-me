package ru.practicum.main.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 255, message = "Длина комментария не может быть меньше 1го или больше 255ти симвлов")
    private String text;
}