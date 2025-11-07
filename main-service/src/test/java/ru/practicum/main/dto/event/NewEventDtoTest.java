package ru.practicum.main.dto.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.location.LocationDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewEventDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String validAnnotation = "This is a valid annotation that meets the minimum " +
            "length requirement of 20 characters";
    private final String validDescription = "This is a valid description that meets the minimum " +
            "length requirement of 20 characters and is long enough to pass validation";
    private final String validTitle = "Valid Event Title";

    @Test
    void newEventDto_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        NewEventDto newEventDto = NewEventDto.builder()
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(5))
                .location(LocationDto.builder().lat(55.7558f).lon(37.6173f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(false)
                .build();

        // When
        String json = objectMapper.writeValueAsString(newEventDto);
        NewEventDto deserializedDto = objectMapper.readValue(json, NewEventDto.class);

        // Then
        assertNotNull(json);
        assertEquals(newEventDto.getTitle(), deserializedDto.getTitle());
        assertEquals(newEventDto.getAnnotation(), deserializedDto.getAnnotation());
        assertEquals(newEventDto.getDescription(), deserializedDto.getDescription());
        assertEquals(newEventDto.getCategory(), deserializedDto.getCategory());
        assertEquals(newEventDto.getPaid(), deserializedDto.getPaid());
        assertEquals(newEventDto.getParticipantLimit(), deserializedDto.getParticipantLimit());
        assertEquals(newEventDto.getRequestModeration(), deserializedDto.getRequestModeration());
    }

    @Test
    void newEventDto_ShouldUseDefaultValues() throws JsonProcessingException {
        // Given - JSON без полей со значениями по умолчанию
        String json = "{" +
                "\"title\":\"Test Event\"," +
                "\"annotation\":\"This is a valid annotation that meets requirements\"," +
                "\"description\":\"This is a valid description that meets all requirements for length\"," +
                "\"category\":1," +
                "\"eventDate\":\"2024-12-31 20:00:00\"" +
                "}";

        // When
        NewEventDto newEventDto = objectMapper.readValue(json, NewEventDto.class);

        // Then - проверяем значения по умолчанию
        assertEquals("Test Event", newEventDto.getTitle());
        assertFalse(newEventDto.getPaid());
        assertEquals(0, newEventDto.getParticipantLimit());
        assertTrue(newEventDto.getRequestModeration());
        assertNotNull(newEventDto.getEventDate());
    }

    @Test
    void newEventDto_ShouldHandleLocation() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"title\":\"Test Event\"," +
                "\"annotation\":\"Valid annotation text for testing\"," +
                "\"description\":\"Valid description text for testing purposes\"," +
                "\"category\":1," +
                "\"eventDate\":\"2024-12-31 20:00:00\"," +
                "\"location\":{\"lat\":55.7558,\"lon\":37.6173}" +
                "}";

        // When
        NewEventDto newEventDto = objectMapper.readValue(json, NewEventDto.class);

        // Then
        assertNotNull(newEventDto.getLocation());
        assertEquals(55.7558f, newEventDto.getLocation().getLat(), 0.0001f);
        assertEquals(37.6173f, newEventDto.getLocation().getLon(), 0.0001f);
    }
}