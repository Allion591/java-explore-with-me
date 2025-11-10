package ru.practicum.main.dto.compilation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompilationDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void compilationDto_ShouldSerializeToJson() throws JsonProcessingException {
        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .events(List.of())
                .build();

        String json = objectMapper.writeValueAsString(compilationDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"title\":\"Summer Events\""));
        assertTrue(json.contains("\"pinned\":true"));
        assertTrue(json.contains("\"events\":[]"));
    }

    @Test
    void compilationDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        String json = "{\"id\":1,\"title\":\"Summer Events\",\"pinned\":true,\"events\":[]}";

        CompilationDto compilationDto = objectMapper.readValue(json, CompilationDto.class);

        assertNotNull(compilationDto);
        assertEquals(1L, compilationDto.getId());
        assertEquals("Summer Events", compilationDto.getTitle());
        assertTrue(compilationDto.getPinned());
        assertNotNull(compilationDto.getEvents());
        assertTrue(compilationDto.getEvents().isEmpty());
    }

    @Test
    void compilationDto_WithEvents_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        EventShortDto event = EventShortDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .build();

        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Music Events")
                .pinned(false)
                .events(List.of(event))
                .build();

        String json = objectMapper.writeValueAsString(compilationDto);
        CompilationDto deserializedDto = objectMapper.readValue(json, CompilationDto.class);

        assertNotNull(json);
        assertEquals(compilationDto.getId(), deserializedDto.getId());
        assertEquals(compilationDto.getTitle(), deserializedDto.getTitle());
        assertEquals(compilationDto.getPinned(), deserializedDto.getPinned());
        assertEquals(1, deserializedDto.getEvents().size());
        assertEquals("Summer Concert", deserializedDto.getEvents().get(0).getTitle());
    }

    @Test
    void compilationDto_ShouldHandleNullValues() throws JsonProcessingException {
        String json = "{\"id\":1,\"title\":null,\"pinned\":null,\"events\":null}";

        CompilationDto compilationDto = objectMapper.readValue(json, CompilationDto.class);

        assertNotNull(compilationDto);
        assertEquals(1L, compilationDto.getId());
        assertNull(compilationDto.getTitle());
        assertNull(compilationDto.getPinned());
        assertNull(compilationDto.getEvents());
    }

    @Test
    void compilationDto_ShouldUseBuilderDefaultForEvents() {
        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .build();

        assertNotNull(compilationDto.getEvents());
        assertTrue(compilationDto.getEvents().isEmpty());
    }

    @Test
    void compilationDto_ShouldHaveLombokFunctionality() {
        CompilationDto compilation1 = CompilationDto.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .events(List.of())
                .build();

        CompilationDto compilation2 = CompilationDto.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .events(List.of())
                .build();

        CompilationDto compilation3 = CompilationDto.builder()
                .id(2L)
                .title("Winter Events")
                .pinned(false)
                .events(List.of())
                .build();

        assertEquals(compilation1, compilation2);
        assertNotEquals(compilation1, compilation3);
        assertEquals(compilation1.hashCode(), compilation2.hashCode());
        assertNotEquals(compilation1.hashCode(), compilation3.hashCode());

        assertNotNull(compilation1.toString());
        assertTrue(compilation1.toString().contains("Summer Events"));

        compilation1.setTitle("Updated Title");
        assertEquals("Updated Title", compilation1.getTitle());

        compilation1.setPinned(false);
        assertFalse(compilation1.getPinned());

        List<EventShortDto> newEvents = List.of(EventShortDto.builder().id(1L).build());
        compilation1.setEvents(newEvents);
        assertEquals(1, compilation1.getEvents().size());
    }

    @Test
    void compilationDto_WithNoArgsConstructor_ShouldCreateObject() {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(1L);
        compilationDto.setTitle("Test");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of());

        assertNotNull(compilationDto);
        assertEquals(1L, compilationDto.getId());
        assertEquals("Test", compilationDto.getTitle());
        assertTrue(compilationDto.getPinned());
        assertNotNull(compilationDto.getEvents());
    }

    @Test
    void compilationDto_WithAllArgsConstructor_ShouldCreateObject() {
        List<EventShortDto> events = List.of();
        CompilationDto compilationDto = new CompilationDto(events, 1L, true, "Test");

        assertNotNull(compilationDto);
        assertEquals(1L, compilationDto.getId());
        assertEquals("Test", compilationDto.getTitle());
        assertTrue(compilationDto.getPinned());
        assertNotNull(compilationDto.getEvents());
    }
}