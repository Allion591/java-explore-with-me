package ru.practicum.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.ViewStats;

import static org.junit.jupiter.api.Assertions.*;

class ViewStatsTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateViewStatsWithAllArgsConstructor() {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 15L);

        assertEquals("ewm-main-service", viewStats.getApp());
        assertEquals("/events/1", viewStats.getUri());
        assertEquals(15L, viewStats.getHits());
    }

    @Test
    void shouldCreateViewStatsWithNoArgsConstructor() {
        ViewStats viewStats = new ViewStats();

        viewStats.setApp("ewm-main-service");
        viewStats.setUri("/events/1");
        viewStats.setHits(15L);

        assertEquals("ewm-main-service", viewStats.getApp());
        assertEquals("/events/1", viewStats.getUri());
        assertEquals(15L, viewStats.getHits());
    }

    @Test
    void shouldSerializeToJson() throws JsonProcessingException {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 15L);

        String json = objectMapper.writeValueAsString(viewStats);

        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
        assertTrue(json.contains("\"uri\":\"/events/1\""));
        assertTrue(json.contains("\"hits\":15"));
    }

    @Test
    void shouldDeserializeFromJson() throws JsonProcessingException {
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
    void shouldHandleEqualsAndHashCode() {
        ViewStats stats1 = new ViewStats("app1", "/uri1", 10L);
        ViewStats stats2 = new ViewStats("app1", "/uri1", 10L);
        ViewStats stats3 = new ViewStats("app2", "/uri2", 20L);

        assertEquals(stats1, stats2, "Объекты с одинаковыми полями должны быть равны");
        assertNotEquals(stats1, stats3, "Объекты с разными полями не должны быть равны");
        assertEquals(stats1.hashCode(), stats2.hashCode(), "HashCode должен быть одинаковым для равных объектов");
    }

    @Test
    void shouldImplementToString() {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 15L);

        String toString = viewStats.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ewm-main-service"));
        assertTrue(toString.contains("/events/1"));
        assertTrue(toString.contains("15"));
    }

    @Test
    void shouldHandleZeroHits() {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 0L);

        assertEquals(0L, viewStats.getHits());
    }

    @Test
    void shouldHandleNullFields() {
        ViewStats viewStats = new ViewStats();

        viewStats.setApp(null);
        viewStats.setUri(null);
        viewStats.setHits(null);

        assertNull(viewStats.getApp());
        assertNull(viewStats.getUri());
        assertNull(viewStats.getHits());
    }
}