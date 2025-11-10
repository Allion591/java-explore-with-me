package ru.practicum.main.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Заголовок подборки не может быть пустым")
    @Size(max = 50, message = "Длина заголовка подборки не может быть меньше 1го или больше 50ти симвлов")
    private String title;
}