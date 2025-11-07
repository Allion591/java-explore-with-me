package ru.practicum.main.controller.publics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.service.interfaces.CompilationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCompilationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    @Test
    void getCompilations_WithPinnedFilter_ShouldReturn200AndFilteredList() throws Exception {
        CompilationDto pinnedCompilation = CompilationDto.builder()
                .id(1L)
                .title("Pinned Events")
                .pinned(true)
                .events(List.of())
                .build();

        when(compilationService.getCompilations(eq(true), anyInt(), anyInt()))
                .thenReturn(List.of(pinnedCompilation));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pinned").value(true));

        verify(compilationService, times(1)).getCompilations(true, 0, 10);
    }

    @Test
    void getCompilations_WithDefaultPagination_ShouldReturn200() throws Exception {
        when(compilationService.getCompilations(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk());

        verify(compilationService, times(1)).getCompilations(null, 0, 10);
    }

    @Test
    void getCompilations_WithInvalidPagination_ShouldReturn400() throws Exception {
        Integer invalidFrom = -1;
        Integer invalidSize = 0;

        mockMvc.perform(get("/compilations")
                        .param("from", invalidFrom.toString())
                        .param("size", invalidSize.toString()))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).getCompilations(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void getCompilation_ShouldReturn200AndCompilation() throws Exception {
        Long compilationId = 1L;

        EventShortDto event = EventShortDto.builder()
                .id(1L)
                .title("Concert")
                .annotation("Amazing concert")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .build();

        CompilationDto compilation = CompilationDto.builder()
                .id(compilationId)
                .title("Music Events")
                .pinned(true)
                .events(List.of(event))
                .build();

        when(compilationService.getCompilation(anyLong())).thenReturn(compilation);

        mockMvc.perform(get("/compilations/{compId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Music Events"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events.length()").value(1))
                .andExpect(jsonPath("$.events[0].title").value("Concert"));

        verify(compilationService, times(1)).getCompilation(compilationId);
    }

    @Test
    void getCompilation_WithInvalidId_ShouldReturn400() throws Exception {
        Long invalidCompilationId = 0L;

        mockMvc.perform(get("/compilations/{compId}", invalidCompilationId))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).getCompilation(anyLong());
    }
}