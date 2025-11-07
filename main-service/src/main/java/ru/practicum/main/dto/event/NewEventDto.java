package ru.practicum.main.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.dto.location.LocationDto;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация не может быть меньше 20ти или больше 2000 символов")
    private String annotation;

    @NotNull(message = "Id категории не может быть пустым")
    @Positive(message = "Id категории должен быть положительным")
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание не может быть меньше 20ти или больше 7000 символов")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    @Builder.Default
    private LocalDateTime eventDate = LocalDateTime.now().plusHours(5);

    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Min(value = 0, message = "Количество участников должно быть положительным")
    @JsonProperty("participantLimit")
    @Builder.Default
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок не может быть меньше 3х или больше 120 символов")
    private String title;
}