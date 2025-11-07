package ru.practicum.main.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250, message = "Имя не может быть меньше 2х или больше 250 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Формат email не допустим")
    @Size(min = 6, max = 254, message = "Email не может быть меньше 6ти или больше 254 символов")
    private String email;
}