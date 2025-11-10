package ru.practicum.main.dto.filter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventPublicFilterRequest {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Boolean onlyAvailable = false;

    @Builder.Default
    private String sort = "EVENT_DATE";

    @PositiveOrZero
    @Builder.Default
    private Integer from = 0;

    @Positive
    @Builder.Default
    private Integer size = 10;

    @AssertTrue(message = "Дата начала должна быть раньше чем дата конца")
    private boolean isRangeValid() {
        return rangeStart == null || rangeEnd == null || !rangeStart.isAfter(rangeEnd);
    }

    public LocalDateTime getEffectiveRangeStart() {
        // Если обе даты не указаны, используем текущее время
        if (rangeStart == null && rangeEnd == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }
}