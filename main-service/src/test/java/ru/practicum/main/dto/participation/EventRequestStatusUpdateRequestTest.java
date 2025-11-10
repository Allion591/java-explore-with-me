package ru.practicum.main.dto.participation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventRequestStatusUpdateRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void eventRequestStatusUpdateRequest_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L, 3L))
                .status("CONFIRMED")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        EventRequestStatusUpdateRequest deserialized = objectMapper.readValue(json, EventRequestStatusUpdateRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"requestIds\":[1,2,3]"));
        assertTrue(json.contains("\"status\":\"CONFIRMED\""));
        assertEquals(request.getRequestIds(), deserialized.getRequestIds());
        assertEquals(request.getStatus(), deserialized.getStatus());
    }

    @Test
    void eventRequestStatusUpdateRequest_ShouldHandleDifferentStatuses() throws JsonProcessingException {
        // Given
        String jsonConfirmed = "{\"requestIds\":[1,2],\"status\":\"CONFIRMED\"}";
        String jsonRejected = "{\"requestIds\":[3,4],\"status\":\"REJECTED\"}";

        // When
        EventRequestStatusUpdateRequest confirmedRequest = objectMapper.readValue(jsonConfirmed, EventRequestStatusUpdateRequest.class);
        EventRequestStatusUpdateRequest rejectedRequest = objectMapper.readValue(jsonRejected, EventRequestStatusUpdateRequest.class);

        // Then
        assertEquals(List.of(1L, 2L), confirmedRequest.getRequestIds());
        assertEquals("CONFIRMED", confirmedRequest.getStatus());
        assertEquals(List.of(3L, 4L), rejectedRequest.getRequestIds());
        assertEquals("REJECTED", rejectedRequest.getStatus());
    }

    @Test
    void eventRequestStatusUpdateRequest_ShouldHaveLombokFunctionality() {
        // Given & When
        EventRequestStatusUpdateRequest request1 = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L))
                .status("CONFIRMED")
                .build();

        EventRequestStatusUpdateRequest request2 = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L))
                .status("CONFIRMED")
                .build();

        EventRequestStatusUpdateRequest request3 = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(3L))
                .status("REJECTED")
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());

        assertNotNull(request1.toString());
        assertTrue(request1.toString().contains("CONFIRMED"));

        // Проверка геттеров и сеттеров
        request1.setRequestIds(List.of(5L, 6L));
        request1.setStatus("REJECTED");
        assertEquals(List.of(5L, 6L), request1.getRequestIds());
        assertEquals("REJECTED", request1.getStatus());
    }
}