package ru.practicum.main.dto.comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.enums.CommentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentAdminRequest {
    @NotNull
    private CommentStatus stateAction;
}