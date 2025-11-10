package ru.practicum.main.dto.compilation;

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
public class UpdateCompilationRequest {
    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @Size(max = 50, message = "Длина заголовка подборки не может быть больше 50ти симвлов")
    private String title;
}