package ru.practicum.main.controller.publics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.service.interfaces.CompilationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicCompilationControllerUnitTest {

    @Mock
    private CompilationService compilationService;

    @InjectMocks
    private PublicCompilationController publicCompilationController;

    @Test
    void getCompilations_ShouldReturnCompilationList() {
        Boolean pinned = true;
        Integer from = 0;
        Integer size = 10;

        EventShortDto event = EventShortDto.builder()
                .id(1L)
                .title("Test Event")
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        CompilationDto compilation1 = CompilationDto.builder()
                .id(1L)
                .title("Top Events")
                .pinned(true)
                .events(List.of(event))
                .build();

        CompilationDto compilation2 = CompilationDto.builder()
                .id(2L)
                .title("Featured Events")
                .pinned(true)
                .events(List.of(event))
                .build();

        when(compilationService.getCompilations(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(compilation1, compilation2));

        List<CompilationDto> result = publicCompilationController.getCompilations(pinned, from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Top Events", result.get(0).getTitle());
        assertTrue(result.get(0).getPinned());
        verify(compilationService, times(1)).getCompilations(pinned, from, size);
    }

    @Test
    void getCompilations_WithNullPinned_ShouldReturnAllCompilations() {
        Integer from = 0;
        Integer size = 10;

        CompilationDto compilation = CompilationDto.builder()
                .id(1L)
                .title("All Events")
                .pinned(false)
                .events(List.of())
                .build();

        when(compilationService.getCompilations(isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(compilation));

        List<CompilationDto> result = publicCompilationController.getCompilations(null, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("All Events", result.get(0).getTitle());
        assertFalse(result.get(0).getPinned());
        verify(compilationService, times(1)).getCompilations(null, from, size);
    }

    @Test
    void getCompilation_ShouldReturnCompilation() {
        Long compilationId = 1L;
        CompilationDto expectedCompilation = CompilationDto.builder()
                .id(compilationId)
                .title("Summer Events")
                .pinned(true)
                .events(List.of())
                .build();

        when(compilationService.getCompilation(anyLong())).thenReturn(expectedCompilation);

        CompilationDto actualCompilation = publicCompilationController.getCompilation(compilationId);

        assertNotNull(actualCompilation);
        assertEquals(compilationId, actualCompilation.getId());
        assertEquals("Summer Events", actualCompilation.getTitle());
        assertTrue(actualCompilation.getPinned());
        verify(compilationService, times(1)).getCompilation(compilationId);
    }
}