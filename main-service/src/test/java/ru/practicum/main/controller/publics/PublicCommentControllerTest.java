package ru.practicum.main.controller.publics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.service.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void getAllCommentsByEventId_ShouldReturnCommentsList() throws Exception {
        Long eventId = 1L;

        CommentResponseDto comment1 = CommentResponseDto.builder()
                .id(1L)
                .text("First approved comment")
                .eventId(eventId)
                .authorId(1L)
                .created(LocalDateTime.now().minusDays(1))
                .state("APPROVED")
                .build();

        CommentResponseDto comment2 = CommentResponseDto.builder()
                .id(2L)
                .text("Second approved comment")
                .eventId(eventId)
                .authorId(2L)
                .created(LocalDateTime.now().minusHours(5))
                .state("APPROVED")
                .build();

        List<CommentResponseDto> response = List.of(comment1, comment2);

        when(commentService.getAllByEventId(any(Long.class), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("First approved comment"))
                .andExpect(jsonPath("$[0].eventId").value(eventId))
                .andExpect(jsonPath("$[0].state").value("APPROVED"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].text").value("Second approved comment"))
                .andExpect(jsonPath("$[1].eventId").value(eventId))
                .andExpect(jsonPath("$[1].state").value("APPROVED"));
    }

    @Test
    void getAllCommentsByEventId_WithDefaultParameters_ShouldReturnCommentsList() throws Exception {
        Long eventId = 1L;

        CommentResponseDto comment = CommentResponseDto.builder()
                .id(1L)
                .text("Test approved comment")
                .eventId(eventId)
                .authorId(1L)
                .created(LocalDateTime.now())
                .state("APPROVED")
                .build();

        List<CommentResponseDto> response = List.of(comment);

        when(commentService.getAllByEventId(eq(eventId), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/comments/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Test approved comment"))
                .andExpect(jsonPath("$[0].eventId").value(eventId))
                .andExpect(jsonPath("$[0].state").value("APPROVED"));
    }

    @Test
    void getAllCommentsByEventId_WhenNoComments_ShouldReturnEmptyList() throws Exception {
        Long eventId = 1L;

        when(commentService.getAllByEventId(any(Long.class), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllCommentsByEventId_WhenInvalidFrom_ShouldReturnBadRequest() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCommentsByEventId_WhenInvalidSize_ShouldReturnBadRequest() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .param("from", "-1")
                        .param("size", "-2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCommentsByEventId_WhenSizeTooLarge_ShouldReturnBadRequest() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(get("/comments/{eventId}", eventId)
                        .param("from", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCommentsByEventId_WhenInvalidEventId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", 0)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}