package ru.practicum.main.dto.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentAdminRequest {
    @NotNull
    @Size(max = 20, message = "Статус не может быть длиннее 20ти символов")
    private String stateAction;
}