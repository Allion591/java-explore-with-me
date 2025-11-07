package ru.practicum.main.dto.participation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParticipationRequestDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void participationRequestDto_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        LocalDateTime created = LocalDateTime.now();
        ParticipationRequestDto request = ParticipationRequestDto.builder()
                .id(1L)
                .event(2L)
                .requester(3L)
                .status("CONFIRMED")
                .created(created)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        ParticipationRequestDto deserialized = objectMapper.readValue(json, ParticipationRequestDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"event\":2"));
        assertTrue(json.contains("\"requester\":3"));
        assertTrue(json.contains("\"status\":\"CONFIRMED\""));
        assertEquals(request.getId(), deserialized.getId());
        assertEquals(request.getEvent(), deserialized.getEvent());
        assertEquals(request.getRequester(), deserialized.getRequester());
        assertEquals(request.getStatus(), deserialized.getStatus());
    }

    @Test
    void participationRequestDto_ShouldHandleJsonFormatForDate() throws JsonProcessingException {
        // Given
        String created = "2024-07-15 14:30:00";
        String json = String.format("{" +
                "\"id\":1," +
                "\"event\":2," +
                "\"requester\":3," +
                "\"status\":\"PENDING\"," +
                "\"created\":\"%s\"" +
                "}", created);

        // When
        ParticipationRequestDto request = objectMapper.readValue(json, ParticipationRequestDto.class);

        // Then - дата должна быть корректно распарсена
        assertNotNull(request);
        assertEquals(LocalDateTime.parse(created, formatter), request.getCreated());
    }

    @Test
    void participationRequestDto_ShouldHaveLombokFunctionality() {
        // Given & When
        LocalDateTime created = LocalDateTime.now();
        ParticipationRequestDto request1 = ParticipationRequestDto.builder()
                .id(1L)
                .event(2L)
                .requester(3L)
                .status("CONFIRMED")
                .created(created)
                .build();

        ParticipationRequestDto request2 = ParticipationRequestDto.builder()
                .id(1L)
                .event(2L)
                .requester(3L)
                .status("CONFIRMED")
                .created(created)
                .build();

        ParticipationRequestDto request3 = ParticipationRequestDto.builder()
                .id(2L)
                .event(3L)
                .requester(4L)
                .status("REJECTED")
                .created(created.plusHours(1))
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());

        assertNotNull(request1.toString());
        assertTrue(request1.toString().contains("CONFIRMED"));

        // Проверка геттеров и сеттеров
        request1.setId(5L);
        request1.setEvent(6L);
        request1.setRequester(7L);
        request1.setStatus("CANCELED");
        request1.setCreated(created.plusDays(1));

        assertEquals(5L, request1.getId());
        assertEquals(6L, request1.getEvent());
        assertEquals(7L, request1.getRequester());
        assertEquals("CANCELED", request1.getStatus());
        assertEquals(created.plusDays(1), request1.getCreated());
    }
}