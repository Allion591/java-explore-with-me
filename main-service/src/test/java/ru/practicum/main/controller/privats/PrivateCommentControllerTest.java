package ru.practicum.main.controller.privats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.service.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PrivateCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void saveComment_ShouldReturnCreated() throws Exception {
        Long userId = 1L;
        Long eventId = 1L;
        CommentDto request = new CommentDto("New comment");
        CommentResponseDto response = CommentResponseDto.builder()
                .id(1L)
                .text("New comment")
                .eventId(eventId)
                .authorId(userId)
                .created(LocalDateTime.now())
                .state("PENDING")
                .build();

        when(commentService.saveComment(any(Long.class), any(Long.class), any(CommentDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("New comment"))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.authorId").value(userId))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void getUserComment_ShouldReturnComment() throws Exception {
        Long userId = 1L;
        Long commentId = 1L;
        CommentResponseDto response = CommentResponseDto.builder()
                .id(commentId)
                .text("Test comment")
                .eventId(1L)
                .authorId(userId)
                .created(LocalDateTime.now().minusHours(1))
                .state("APPROVED")
                .build();

        when(commentService.getCommentById(any(Long.class), any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(get("/users/{userId}/comments/{commentId}", userId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorId").value(userId))
                .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    void getUserComments_ShouldReturnList() throws Exception {
        Long userId = 1L;
        CommentResponseDto comment1 = CommentResponseDto.builder()
                .id(1L)
                .text("First comment")
                .eventId(1L)
                .authorId(userId)
                .created(LocalDateTime.now().minusDays(1))
                .state("PENDING")
                .build();

        CommentResponseDto comment2 = CommentResponseDto.builder()
                .id(2L)
                .text("Second comment")
                .eventId(2L)
                .authorId(userId)
                .created(LocalDateTime.now().minusHours(5))
                .state("APPROVED")
                .build();

        List<CommentResponseDto> response = List.of(comment1, comment2);

        when(commentService.getUserComments(any(Long.class), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/users/{userId}/comments", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("First comment"))
                .andExpect(jsonPath("$[0].state").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].text").value("Second comment"))
                .andExpect(jsonPath("$[1].state").value("APPROVED"));
    }

    @Test
    void getUserComments_WithDefaultParameters_ShouldReturnList() throws Exception {
        Long userId = 1L;
        CommentResponseDto comment = CommentResponseDto.builder()
                .id(1L)
                .text("Test comment")
                .eventId(1L)
                .authorId(userId)
                .created(LocalDateTime.now())
                .state("PENDING")
                .build();

        List<CommentResponseDto> response = List.of(comment);

        when(commentService.getUserComments(eq(userId), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/users/{userId}/comments", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Test comment"));
    }

    @Test
    void updateComment_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        Long commentId = 1L;
        CommentDto request = new CommentDto("Updated comment");
        CommentResponseDto response = CommentResponseDto.builder()
                .id(commentId)
                .text("Updated comment")
                .eventId(1L)
                .authorId(userId)
                .created(LocalDateTime.now().minusHours(1))
                .updated(LocalDateTime.now())
                .state("PENDING")
                .build();

        when(commentService.updateCommentByUser(any(Long.class), any(Long.class), any(CommentDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Updated comment"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void deleteComment_ShouldReturnNoContent() throws Exception {
        Long userId = 1L;
        Long commentId = 1L;

        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", userId, commentId))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(userId, commentId);
    }
}