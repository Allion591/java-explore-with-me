package ru.practicum.main.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории не может быть меньше 1го или больше 50ти символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-_,.!?;:()]+$",
            message = "Название категории содержит запрещенные символы")
    private String name;
}