package ru.practicum.main.controller.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.main.service.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void updateStatusComment_ShouldReturnOk() throws Exception {
        Long commentId = 1L;
        UpdateCommentAdminRequest request = new UpdateCommentAdminRequest("APPROVE");

        CommentResponseDto response = CommentResponseDto.builder()
                .id(commentId)
                .text("Обновленный комментарий")
                .eventId(10L)
                .authorId(5L)
                .created(LocalDateTime.now().minusHours(2))
                .updated(LocalDateTime.now())
                .state("APPROVED")
                .build();

        when(commentService.updateCommentByAdmin(any(Long.class), any(UpdateCommentAdminRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Обновленный комментарий"))
                .andExpect(jsonPath("$.eventId").value(10L))
                .andExpect(jsonPath("$.authorId").value(5L))
                .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    void getAllPendingComments_ShouldReturnList() throws Exception {
        CommentResponseDto comment1 = CommentResponseDto.builder()
                .id(1L)
                .text("Первый комментарий на модерации")
                .eventId(10L)
                .authorId(5L)
                .created(LocalDateTime.now().minusDays(1))
                .state("PENDING")
                .build();

        CommentResponseDto comment2 = CommentResponseDto.builder()
                .id(2L)
                .text("Второй комментарий на модерации")
                .eventId(11L)
                .authorId(6L)
                .created(LocalDateTime.now().minusHours(5))
                .state("PENDING")
                .build();

        List<CommentResponseDto> response = List.of(comment1, comment2);

        when(commentService.getCommentsForModeration(anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/admin/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Первый комментарий на модерации"))
                .andExpect(jsonPath("$[0].state").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].text").value("Второй комментарий на модерации"))
                .andExpect(jsonPath("$[1].state").value("PENDING"));
    }

    @Test
    void getAllPendingComments_WithDefaultParameters_ShouldReturnList() throws Exception {
        CommentResponseDto comment = CommentResponseDto.builder()
                .id(1L)
                .text("Комментарий по умолчанию")
                .eventId(10L)
                .authorId(5L)
                .created(LocalDateTime.now().minusDays(1))
                .state("PENDING")
                .build();

        List<CommentResponseDto> response = List.of(comment);

        when(commentService.getCommentsForModeration(0, 10))
                .thenReturn(response);

        mockMvc.perform(get("/admin/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Комментарий по умолчанию"));
    }
}