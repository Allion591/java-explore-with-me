package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    private Long id;

    @NotBlank(message = "Название микросервиса не может быть пустым")
    @Size(max = 255, message = "Имя приложения не может быть длиннее 255 символов")
    private String app;

    @NotBlank(message = "URI запроса не может быть пустым")
    @Size(max = 500, message = "URI не может быть длиннее 500 символов")
    private String uri;

    @NotBlank(message = "Неверный формат ip адреса")
    @Pattern(regexp = "^([0-9a-fA-F.:]+|localhost)$",
            message = "Неверный формат ip адреса")
    private String ip;

    @NotNull(message = "Дата и время запроса не указано")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}