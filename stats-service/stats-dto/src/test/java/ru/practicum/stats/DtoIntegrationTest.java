package ru.practicum.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoIntegrationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeEndpointHit() throws JsonProcessingException {
        EndpointHit hit = new EndpointHit();
        hit.setId(1L);
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 30, 45));

        String json = objectMapper.writeValueAsString(hit);

        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
        assertTrue(json.contains("\"uri\":\"/events/1\""));
        assertTrue(json.contains("\"ip\":\"192.168.1.1\""));
        assertTrue(json.contains("\"timestamp\":\"2024-01-01 10:30:45\""));
    }

    @Test
    void shouldDeserializeEndpointHit() throws IOException {
        String json = "{"
                + "\"id\": 1, "
                + "\"app\": \"ewm-main-service\", "
                + "\"uri\": \"/events/1\", "
                + "\"ip\": \"192.168.1.1\", "
                + "\"timestamp\": \"2024-01-01 10:30:45\""
                + "}";

        EndpointHit hit = objectMapper.readValue(json, EndpointHit.class);

        assertEquals(1L, hit.getId());
        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events/1", hit.getUri());
        assertEquals("192.168.1.1", hit.getIp());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30, 45),
                hit.getTimestamp());
    }

    @Test
    void shouldSerializeViewStats() throws JsonProcessingException {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 15L);

        String json = objectMapper.writeValueAsString(viewStats);

        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
        assertTrue(json.contains("\"uri\":\"/events/1\""));
        assertTrue(json.contains("\"hits\":15"));
    }

    @Test
    void shouldDeserializeViewStats() throws IOException {

        String json = "{"
                + "\"app\": \"ewm-main-service\", "
                + "\"uri\": \"/events/1\", "
                + "\"hits\": 15"
                + "}";

        ViewStats viewStats = objectMapper.readValue(json, ViewStats.class);

        assertEquals("ewm-main-service", viewStats.getApp());
        assertEquals("/events/1", viewStats.getUri());
        assertEquals(15L, viewStats.getHits());
    }

    @Test
    void shouldHandleEndpointHitWithNullId() throws JsonProcessingException {

        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());

        String json = objectMapper.writeValueAsString(hit);

        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
    }
}