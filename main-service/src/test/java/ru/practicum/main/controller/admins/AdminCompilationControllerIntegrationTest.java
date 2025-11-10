package ru.practicum.main.controller.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.service.interfaces.CompilationService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCompilationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    @Test
    void createCompilation_ShouldReturn201AndCompilationDto() throws Exception {

        NewCompilationDto request = NewCompilationDto.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptyList())
                .build();
        CompilationDto response = CompilationDto.builder()
                .id(1L)
                .title("Test Compilation")
                .events(Collections.emptyList())
                .pinned(true)
                .build();

        when(compilationService.createCompilation(any(NewCompilationDto.class))).thenReturn(response);


        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Compilation"))
                .andExpect(jsonPath("$.pinned").value(true));

        verify(compilationService, times(1)).createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void createCompilation_WithInvalidData_ShouldReturn400() throws Exception {
        NewCompilationDto invalidRequest = NewCompilationDto.builder()
                .title("")
                .pinned(true)
                .events(Collections.emptyList())
                .build();


        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).createCompilation(any());
    }

    @Test
    void deleteCompilation_ShouldReturn204() throws Exception {

        Long compilationId = 1L;
        doNothing().when(compilationService).deleteCompilation(compilationId);


        mockMvc.perform(delete("/admin/compilations/{compId}", compilationId))
                .andExpect(status().isNoContent());

        verify(compilationService, times(1)).deleteCompilation(compilationId);
    }

    @Test
    void deleteCompilation_WithInvalidId_ShouldReturn400() throws Exception {
        Long invalidCompilationId = 0L;


        mockMvc.perform(delete("/admin/compilations/{compId}", invalidCompilationId))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).deleteCompilation(anyLong());
    }

    @Test
    void updateCompilation_ShouldReturn200AndUpdatedCompilation() throws Exception {

        Long compilationId = 1L;
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Updated Title")
                .pinned(true)
                .events(Collections.emptyList())
                .build();
        CompilationDto response = CompilationDto.builder()
                .id(1L)
                .title("Updated Title")
                .events(Collections.emptyList())
                .build();

        when(compilationService.updateCompilation(anyLong(), any(UpdateCompilationRequest.class)))
                .thenReturn(response);


        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(compilationService, times(1)).updateCompilation(anyLong(),
                any(UpdateCompilationRequest.class));
    }

    @Test
    void updateCompilation_WithInvalidId_ShouldReturn400() throws Exception {

        Long invalidCompilationId = 0L;
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptyList())
                .build();


        mockMvc.perform(patch("/admin/compilations/{compId}", invalidCompilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).updateCompilation(anyLong(), any());
    }
}