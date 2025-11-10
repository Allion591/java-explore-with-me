package ru.practicum.main.dto.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.main.enums.EventState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEventFilterParams {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

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

    @JsonCreator
    public static AdminEventFilterParams create(
            @JsonProperty("users") List<Long> users,
            @JsonProperty("states") List<String> stateStrings,
            @JsonProperty("categories") List<Long> categories,
            @JsonProperty("rangeStart") LocalDateTime rangeStart,
            @JsonProperty("rangeEnd") LocalDateTime rangeEnd,
            @JsonProperty("from") Integer from,
            @JsonProperty("size") Integer size) {

        List<EventState> states = null;
        if (stateStrings != null) {
            states = stateStrings.stream()
                    .map(String::toUpperCase)
                    .map(stateStr -> {
                        try {
                            return EventState.valueOf(stateStr);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return AdminEventFilterParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from != null ? from : 0)
                .size(size != null ? size : 10)
                .build();
    }
}