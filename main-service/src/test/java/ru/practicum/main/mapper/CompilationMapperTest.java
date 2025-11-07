package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.model.Compilation;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompilationMapperTest {

    @Autowired
    private CompilationMapper compilationMapper;

    @Test
    void toCompilationDto_ShouldMapCompilationToCompilationDto() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        // When
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);

        // Then
        assertNotNull(compilationDto);
        assertEquals(compilation.getId(), compilationDto.getId());
        assertEquals(compilation.getTitle(), compilationDto.getTitle());
        assertEquals(compilation.getPinned(), compilationDto.getPinned());
        assertNotNull(compilationDto.getEvents());
    }

    @Test
    void toCompilation_ShouldMapNewCompilationDtoToCompilation() {
        // Given
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title("Winter Events")
                .pinned(true)
                .events(Collections.emptyList())
                .build();

        // When
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        // Then
        assertNotNull(compilation);
        assertNull(compilation.getId()); // id должен быть проигнорирован
        assertEquals(newCompilationDto.getTitle(), compilation.getTitle());
        assertEquals(newCompilationDto.getPinned(), compilation.getPinned());
    }

    @Test
    void updateCompilationFromRequest_ShouldUpdateFields() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Old Title")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("New Title")
                .pinned(true)
                .build();

        // When
        compilationMapper.updateCompilationFromRequest(updateRequest, compilation);

        // Then
        assertEquals(1L, compilation.getId()); // id не должен измениться
        assertEquals("New Title", compilation.getTitle());
        assertTrue(compilation.getPinned());
        assertNotNull(compilation.getEvents());
    }

    @Test
    void updateCompilationFromRequest_ShouldIgnoreNullFields() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Original Title")
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title(null) // null поле
                .pinned(false)
                .build();

        // When
        compilationMapper.updateCompilationFromRequest(updateRequest, compilation);

        // Then - title не должен измениться, так как в запросе null
        assertEquals("Original Title", compilation.getTitle());
        assertFalse(compilation.getPinned()); // pinned должен обновиться
    }

    @Test
    void toCompilationDto_ShouldHandleNull() {
        // When
        CompilationDto compilationDto = compilationMapper.toCompilationDto(null);

        // Then
        assertNull(compilationDto);
    }

    @Test
    void toCompilation_ShouldHandleNull() {
        // When
        Compilation compilation = compilationMapper.toCompilation(null);

        // Then
        assertNull(compilation);
    }

    @Test
    void updateCompilationFromRequest_ShouldHandleNullRequest() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test")
                .pinned(true)
                .build();

        // When
        compilationMapper.updateCompilationFromRequest(null, compilation);

        // Then - compilation не должен измениться
        assertEquals(1L, compilation.getId());
        assertEquals("Test", compilation.getTitle());
        assertTrue(compilation.getPinned());
    }
}