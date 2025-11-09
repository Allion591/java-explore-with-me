package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.statsClient.StatsClient;
import ru.practicum.stats.dto.EndpointHit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StatsClientIntegrationTest {

    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private ObjectMapper objectMapper;
    private String baseUrl;
    private final String appName = "ewm-main-service";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        statsClient = new StatsClient(baseUrl, appName);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void recordHit_shouldSuccessfullySendHitToServer() throws Exception {
        String uri = "/events/1";
        String ip = "192.168.1.1";

        String expectedResponse = objectMapper.writeValueAsString(
                EndpointHit.builder()
                        .app(appName)
                        .uri(uri)
                        .ip(ip)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201));

        // Вызываем новый метод recordHit
        statsClient.recordHit(uri, ip);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/hit", recordedRequest.getPath());
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));

        // Проверяем тело запроса
        String requestBody = recordedRequest.getBody().readUtf8();
        EndpointHit sentHit = objectMapper.readValue(requestBody, EndpointHit.class);
        assertEquals(appName, sentHit.getApp());
        assertEquals(uri, sentHit.getUri());
        assertEquals(ip, sentHit.getIp());
        assertNotNull(sentHit.getTimestamp());
    }

    @Test
    void getEventsViews_shouldSuccessfullyRetrieveStatsFromServer() throws Exception {
        Set<Long> eventIds = Set.of(1L, 2L);
        boolean unique = true;

        String expectedResponse = "["
                + "{\"app\": \"ewm-main-service\", \"uri\": \"/events/1\", \"hits\": 15},"
                + "{\"app\": \"ewm-main-service\", \"uri\": \"/events/2\", \"hits\": 8}"
                + "]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json"));

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, unique);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());

        String requestPath = recordedRequest.getPath();
        assertTrue(requestPath.startsWith("/stats?"));
        assertTrue(requestPath.contains("uris=%2Fevents%2F1"));
        assertTrue(requestPath.contains("uris=%2Fevents%2F2"));
        assertTrue(requestPath.contains("unique=true"));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(15L, result.get(1L));
        assertEquals(8L, result.get(2L));
    }

    @Test
    void getEventViews_shouldReturnCorrectViews() throws Exception {
        Long eventId = 1L;
        boolean unique = true;

        String expectedResponse = "["
                + "{\"app\": \"ewm-main-service\", \"uri\": \"/events/1\", \"hits\": 25}"
                + "]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json"));

        Long result = statsClient.getEventViews(eventId, unique);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestPath = recordedRequest.getPath();
        assertTrue(requestPath.contains("uris=%2Fevents%2F1"));
        assertTrue(requestPath.contains("unique=true"));

        assertEquals(25L, result);
    }

    @Test
    void getEventsViews_withEmptyEventIds_shouldReturnEmptyMapWithoutCallingServer() {
        Set<Long> eventIds = Set.of();
        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void getEventViews_whenEventNotFound_shouldReturnZero() throws Exception {
        Long eventId = 999L;
        boolean unique = true;

        // Сервер возвращает пустой список
        String expectedResponse = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json"));

        Long result = statsClient.getEventViews(eventId, unique);

        assertEquals(0L, result);
    }

    @Test
    void getEventsViews_shouldHandleServerErrorGracefully() {
        Set<Long> eventIds = Set.of(1L, 2L);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        // Должен вернуть пустую map при ошибке
        assertTrue(result.isEmpty());
    }

    @Test
    void getEventViews_shouldHandleServerErrorGracefully() {
        Long eventId = 1L;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        Long result = statsClient.getEventViews(eventId, true);

        // Должен вернуть 0 при ошибке
        assertEquals(0L, result);
    }

    @Test
    void getEventsViews_withUniqueFalse_shouldBuildCorrectUri() throws Exception {
        Set<Long> eventIds = Set.of(1L);
        boolean unique = false;

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .setHeader("Content-Type", "application/json"));

        statsClient.getEventsViews(eventIds, unique);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestPath = recordedRequest.getPath();
        assertTrue(requestPath.contains("unique=false"));
    }

    @Test
    void getEventsViews_shouldHandleMalformedJsonGracefully() {
        Set<Long> eventIds = Set.of(1L);

        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json")
                .setHeader("Content-Type", "application/json"));

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsViews_shouldHandleConnectionErrorGracefully() throws IOException {
        Set<Long> eventIds = Set.of(1L);

        // Останавливаем сервер до отправки запроса
        mockWebServer.shutdown();

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsViews_shouldUseDefaultTimeRange() throws Exception {
        Set<Long> eventIds = Set.of(1L);

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .setHeader("Content-Type", "application/json"));

        statsClient.getEventsViews(eventIds, true);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestPath = recordedRequest.getPath();

        // Должен содержать параметры start и end с датами
        assertTrue(requestPath.contains("start="));
        assertTrue(requestPath.contains("end="));
    }
}