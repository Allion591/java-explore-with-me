package ru.practicum.main.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Аннотация не может быть меньше 20ти или больше 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание не может быть меньше 20ти или больше 7000 символов")
    private String description;

    @Future(message = "Дата начала события должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;
    private Boolean paid;

    @Min(value = 0, message = "Количество участников должно быть положительным")
    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @Builder.Default
    private Boolean requestModeration = true;
    private String stateAction;

    @Size(min = 3, max = 120, message = "Заголовок не может быть меньше 3х или больше 120 символов")
    private String title;
}