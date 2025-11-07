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
class UpdateEventAdminRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String validAnnotation = "This is a valid annotation that meets the minimum length requirement";
    private final String validDescription = "This is a valid description that meets the minimum length requirement";
    private final String validTitle = "Valid Event Title";

    @Test
    void updateEventAdminRequest_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title(validTitle)
                .annotation(validAnnotation)
                .description(validDescription)
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(5))
                .location(LocationDto.builder().lat(55.7558f).lon(37.6173f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(false)
                .stateAction("PUBLISH_EVENT")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        UpdateEventAdminRequest deserialized = objectMapper.readValue(json, UpdateEventAdminRequest.class);

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
    void updateEventAdminRequest_ShouldHandlePartialUpdate() throws JsonProcessingException {
        // Given - только некоторые поля для обновления
        String json = "{" +
                "\"title\":\"Updated Title\"," +
                "\"stateAction\":\"REJECT_EVENT\"" +
                "}";

        // When
        UpdateEventAdminRequest request = objectMapper.readValue(json, UpdateEventAdminRequest.class);

        // Then - только указанные поля должны быть заполнены
        assertEquals("Updated Title", request.getTitle());
        assertEquals("REJECT_EVENT", request.getStateAction());
        assertNull(request.getAnnotation());
        assertNull(request.getDescription());
        assertNull(request.getCategory());
        assertNull(request.getEventDate());
        assertNull(request.getLocation());
        assertNull(request.getPaid());
        assertNull(request.getParticipantLimit());
        assertNull(request.getRequestModeration());
    }

    @Test
    void updateEventAdminRequest_JsonPropertyForParticipantLimit() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"title\":\"Test Event\"," +
                "\"participantLimit\":50" + // Используем имя из @JsonProperty
                "}";

        // When
        UpdateEventAdminRequest request = objectMapper.readValue(json, UpdateEventAdminRequest.class);

        // Then - поле должно быть корректно десериализовано
        assertEquals("Test Event", request.getTitle());
        assertEquals(50, request.getParticipantLimit());
    }
}