package ru.practicum.stats.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StatsRequest {
    @NotNull(message = "Параметр 'start' является обязательным")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "Параметр 'end' является обязательным")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private List<String> uris;

    @Builder.Default
    private Boolean unique = false;

    @AssertTrue(message = "Дата начала должна быть раньше даты окончания")
    public boolean isDateRangeValid() {
        return start == null || end == null || start.isBefore(end);
    }
}