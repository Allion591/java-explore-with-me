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
class UpdateEventUserRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String validAnnotation = "This is a valid annotation that meets the minimum length requirement";
    private final String validDescription = "This is a valid description that meets the minimum length requirement";
    private final String validTitle = "Valid Event Title";

    @Test
    void updateEventUserRequest_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(5))
                .location(LocationDto.builder().lat(55.7558f).lon(37.6173f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(false)
                .stateAction("SEND_TO_REVIEW")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        UpdateEventUserRequest deserialized = objectMapper.readValue(json, UpdateEventUserRequest.class);

        // Then
        assertNotNull(json);
        assertEquals(request.getTitle(), deserialized.getTitle());
        assertEquals(request.getAnnotation(), deserialized.getAnnotation());
        assertEquals(request.getDescription(), deserialized.getDescription());
        assertEquals(request.getCategory(), deserialized.getCategory());
        assertEquals(request.getPaid(), deserialized.getPaid());
        assertEquals(request.getParticipantLimit(), deserialized.getParticipantLimit());
        assertEquals(request.getRequestModeration(), deserialized.getRequestModeration());
        assertEquals(request.getStateAction(), deserialized.getStateAction());
    }

    @Test
    void updateEventUserRequest_ShouldUseDefaultRequestModeration() throws JsonProcessingException {
        // Given - JSON без поля requestModeration
        String json = "{" +
                "\"title\":\"Updated Title\"," +
                "\"stateAction\":\"CANCEL_REVIEW\"" +
                "}";

        // When
        UpdateEventUserRequest request = objectMapper.readValue(json, UpdateEventUserRequest.class);

        // Then - requestModeration должен быть true по умолчанию
        assertEquals("Updated Title", request.getTitle());
        assertEquals("CANCEL_REVIEW", request.getStateAction());
        assertTrue(request.getRequestModeration());
    }

    @Test
    void updateEventUserRequest_ShouldHandlePartialUpdate() throws JsonProcessingException {
        // Given - только некоторые поля для обновления
        String json = "{" +
                "\"title\":\"New Title\"," +
                "\"paid\":true" +
                "}";

        // When
        UpdateEventUserRequest request = objectMapper.readValue(json, UpdateEventUserRequest.class);

        // Then - только указанные поля должны быть заполнены
        assertEquals("New Title", request.getTitle());
        assertTrue(request.getPaid());
        assertNull(request.getAnnotation());
        assertNull(request.getDescription());
        assertNull(request.getCategory());
        assertNull(request.getEventDate());
        assertNull(request.getLocation());
        assertNull(request.getParticipantLimit());
        assertNull(request.getStateAction());
        assertTrue(request.getRequestModeration()); // значение по умолчанию
    }
}