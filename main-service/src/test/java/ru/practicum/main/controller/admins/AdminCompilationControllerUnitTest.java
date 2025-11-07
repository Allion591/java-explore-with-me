package ru.practicum.main.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.controller.admins.AdminCompilationController;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.service.interfaces.CompilationService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCompilationControllerUnitTest {

    @Mock
    private CompilationService compilationService;

    @InjectMocks
    private AdminCompilationController adminCompilationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createCompilation_ShouldReturnCreatedCompilation() {
        NewCompilationDto request = NewCompilationDto.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptyList())
                .build();
        CompilationDto expectedResponse = CompilationDto.builder()
                .id(1L)
                .title("Test Compilation")
                .events(Collections.emptyList())
                .build();

        when(compilationService.createCompilation(any(NewCompilationDto.class))).thenReturn(expectedResponse);

        CompilationDto actualResponse = adminCompilationController.createCompilation(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());
        verify(compilationService, times(1)).createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void deleteCompilation_ShouldCallService() {
        Long compilationId = 1L;
        doNothing().when(compilationService).deleteCompilation(compilationId);

        assertDoesNotThrow(() -> adminCompilationController.deleteCompilation(compilationId));
        verify(compilationService, times(1)).deleteCompilation(compilationId);
    }

    @Test
    void updateCompilation_ShouldReturnUpdatedCompilation() {
        Long compilationId = 1L;
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptyList())
                .build();
        CompilationDto expectedResponse = CompilationDto.builder()
                .id(1L)
                .title("Test Compilation")
                .events(Collections.emptyList())
                .build();

        when(compilationService.updateCompilation(anyLong(), any(UpdateCompilationRequest.class)))
                .thenReturn(expectedResponse);

        CompilationDto actualResponse = adminCompilationController.updateCompilation(compilationId, request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());
        verify(compilationService, times(1)).updateCompilation(anyLong(),
                any(UpdateCompilationRequest.class));
    }
}