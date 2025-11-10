package ru.practicum.main.dto.compilation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewCompilationDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void newCompilationDto_ShouldSerializeToJson() throws JsonProcessingException {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .title("Summer Events")
                .pinned(true)
                .events(List.of(1L, 2L, 3L))
                .build();

        String json = objectMapper.writeValueAsString(newCompilationDto);

        assertNotNull(json);
        assertTrue(json.contains("\"title\":\"Summer Events\""));
        assertTrue(json.contains("\"pinned\":true"));
        assertTrue(json.contains("\"events\":[1,2,3]"));
    }

    @Test
    void newCompilationDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        String json = "{\"title\":\"Summer Events\",\"pinned\":true,\"events\":[1,2,3]}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertEquals("Summer Events", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertNotNull(newCompilationDto.getEvents());
        assertEquals(3, newCompilationDto.getEvents().size());
        assertEquals(List.of(1L, 2L, 3L), newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_ShouldUseDefaultPinnedValue() throws JsonProcessingException {
        String json = "{\"title\":\"Summer Events\",\"events\":[1,2,3]}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertEquals("Summer Events", newCompilationDto.getTitle());
        assertFalse(newCompilationDto.getPinned());
        assertEquals(List.of(1L, 2L, 3L), newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_ShouldHandleNullEvents() throws JsonProcessingException {
        String json = "{\"title\":\"Summer Events\",\"pinned\":true,\"events\":null}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertEquals("Summer Events", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertNull(newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_ShouldHandleEmptyEvents() throws JsonProcessingException {
        String json = "{\"title\":\"Summer Events\",\"pinned\":true,\"events\":[]}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertEquals("Summer Events", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertNotNull(newCompilationDto.getEvents());
        assertTrue(newCompilationDto.getEvents().isEmpty());
    }

    @Test
    void newCompilationDto_ShouldIgnoreUnknownProperties() throws JsonProcessingException {
        String json = "{\"title\":\"Summer Events\",\"pinned\":true,\"events\":[1,2],\"id\":5,\"unknown\":\"value\"}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertEquals("Summer Events", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertEquals(List.of(1L, 2L), newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_ShouldHaveLombokFunctionality() {
        NewCompilationDto dto1 = NewCompilationDto.builder()
                .title("Summer Events")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();

        NewCompilationDto dto2 = NewCompilationDto.builder()
                .title("Summer Events")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();

        NewCompilationDto dto3 = NewCompilationDto.builder()
                .title("Winter Events")
                .pinned(false)
                .events(List.of(3L))
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("Summer Events"));

        dto1.setTitle("Updated Title");
        assertEquals("Updated Title", dto1.getTitle());

        dto1.setPinned(false);
        assertFalse(dto1.getPinned());

        List<Long> newEvents = List.of(4L, 5L);
        dto1.setEvents(newEvents);
        assertEquals(newEvents, dto1.getEvents());
    }

    @Test
    void newCompilationDto_WithNoArgsConstructor_ShouldCreateObject() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("Test");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(List.of(1L));

        assertNotNull(newCompilationDto);
        assertEquals("Test", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertEquals(List.of(1L), newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_WithAllArgsConstructor_ShouldCreateObject() {
        List<Long> events = List.of(1L, 2L);
        NewCompilationDto newCompilationDto = new NewCompilationDto(events, true, "Test");

        assertNotNull(newCompilationDto);
        assertEquals("Test", newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertEquals(events, newCompilationDto.getEvents());
    }

    @Test
    void newCompilationDto_ShouldHandleNullTitle() throws JsonProcessingException {
        String json = "{\"title\":null,\"pinned\":true,\"events\":[1]}";

        NewCompilationDto newCompilationDto = objectMapper.readValue(json, NewCompilationDto.class);

        assertNotNull(newCompilationDto);
        assertNull(newCompilationDto.getTitle());
        assertTrue(newCompilationDto.getPinned());
        assertEquals(List.of(1L), newCompilationDto.getEvents());
    }
}