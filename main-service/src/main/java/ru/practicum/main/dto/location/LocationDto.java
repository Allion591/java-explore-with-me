package ru.practicum.main.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    @NotNull(message = "Широта координат не может быть пустой")
    private Float lat;

    @NotNull(message = "Долгота координат не может быть пустой")
    private Float lon;
}