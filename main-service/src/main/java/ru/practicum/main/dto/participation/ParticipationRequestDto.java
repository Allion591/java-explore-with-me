package ru.practicum.main.dto.participation;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @NotNull(message = "Id события, в котором участвует пользователь, не может быть пустым")
    @Positive
    private Long event;

    private Long id;

    @NotNull(message = "Id заказчика, не может быть пустым")
    @Positive
    private Long requester;

    private String status;
}