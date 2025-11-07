package ru.practicum.main.dto.participation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventRequestStatusUpdateResultTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void eventRequestStatusUpdateResult_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        ParticipationRequestDto confirmedRequest = ParticipationRequestDto.builder()
                .id(1L)
                .requester(2L)
                .event(3L)
                .status("CONFIRMED")
                .created(LocalDateTime.now())
                .build();

        ParticipationRequestDto rejectedRequest = ParticipationRequestDto.builder()
                .id(4L)
                .requester(5L)
                .event(3L)
                .status("REJECTED")
                .created(LocalDateTime.now())
                .build();

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmedRequest))
                .rejectedRequests(List.of(rejectedRequest))
                .build();

        // When
        String json = objectMapper.writeValueAsString(result);
        EventRequestStatusUpdateResult deserialized = objectMapper.readValue(json, EventRequestStatusUpdateResult.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"confirmedRequests\""));
        assertTrue(json.contains("\"rejectedRequests\""));
        assertEquals(1, deserialized.getConfirmedRequests().size());
        assertEquals(1, deserialized.getRejectedRequests().size());
        assertEquals("CONFIRMED", deserialized.getConfirmedRequests().get(0).getStatus());
        assertEquals("REJECTED", deserialized.getRejectedRequests().get(0).getStatus());
    }

    @Test
    void eventRequestStatusUpdateResult_ShouldUseDefaultEmptyLists() {
        // When
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder().build();

        // Then - списки должны быть пустыми по умолчанию
        assertNotNull(result.getConfirmedRequests());
        assertNotNull(result.getRejectedRequests());
        assertTrue(result.getConfirmedRequests().isEmpty());
        assertTrue(result.getRejectedRequests().isEmpty());
    }

    @Test
    void eventRequestStatusUpdateResult_ShouldHandleEmptyResult() throws JsonProcessingException {
        // Given
        String json = "{}";

        // When
        EventRequestStatusUpdateResult result = objectMapper.readValue(json, EventRequestStatusUpdateResult.class);

        // Then - списки должны быть пустыми
        assertNotNull(result.getConfirmedRequests());
        assertNotNull(result.getRejectedRequests());
        assertTrue(result.getConfirmedRequests().isEmpty());
        assertTrue(result.getRejectedRequests().isEmpty());
    }

    @Test
    void eventRequestStatusUpdateResult_ShouldHaveLombokFunctionality() {
        // Given & When
        EventRequestStatusUpdateResult result1 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(List.of())
                .build();

        EventRequestStatusUpdateResult result2 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(List.of())
                .build();

        ParticipationRequestDto request = ParticipationRequestDto.builder().id(1L).build();
        EventRequestStatusUpdateResult result3 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(request))
                .rejectedRequests(List.of())
                .build();

        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());

        assertNotNull(result1.toString());

        // Проверка геттеров и сеттеров
        List<ParticipationRequestDto> newConfirmed = List.of(ParticipationRequestDto.builder().id(5L).build());
        List<ParticipationRequestDto> newRejected = List.of(ParticipationRequestDto.builder().id(6L).build());

        result1.setConfirmedRequests(newConfirmed);
        result1.setRejectedRequests(newRejected);

        assertEquals(newConfirmed, result1.getConfirmedRequests());
        assertEquals(newRejected, result1.getRejectedRequests());
    }
}