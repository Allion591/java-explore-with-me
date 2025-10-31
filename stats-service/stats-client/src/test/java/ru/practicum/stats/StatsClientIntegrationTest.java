package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.StatsClient.StatsClient;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.StatsRequest;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsClientException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsClientIntegrationTest {

    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        statsClient = new StatsClient(objectMapper, baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void postHit_shouldSuccessfullySendHitToServer() throws Exception {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setIp("192.168.1.1");
        hit.setUri("/events/1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        String expectedResponse = "{"
                + "\"app\": \"ewm-main-service\", "
                + "\"uri\": \"/events/1\", "
                + "\"ip\": \"192.168.1.1\", "
                + "\"timestamp\": \"2024-01-01 10:00:00\""
                + "}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201));

        EndpointHit result = statsClient.postHit(hit);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/hit", recordedRequest.getPath());
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));

        // Проверяем тело запроса
        String requestBody = recordedRequest.getBody().readUtf8();
        EndpointHit sentHit = objectMapper.readValue(requestBody, EndpointHit.class);
        assertEquals("ewm-main-service", sentHit.getApp());
        assertEquals("/events/1", sentHit.getUri());

        // Проверяем ответ
        assertNotNull(result);
        assertEquals("ewm-main-service", result.getApp());
        assertEquals("/events/1", result.getUri());
    }

    @Test
    void getStats_shouldSuccessfullyRetrieveStatsFromServer() throws Exception {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.of(2024, 1, 1, 0, 0))
                .end(LocalDateTime.of(2024, 1, 2, 0, 0))
                .uris(List.of("/events/1", "/events/2"))
                .unique(true)
                .build();

        String expectedResponse = "["
                + "{"
                + "\"app\": \"ewm-main-service\", "
                + "\"uri\": \"/events/1\", "
                + "\"hits\": 15"
                + "},"
                + "{"
                + "\"app\": \"ewm-main-service\", "
                + "\"uri\": \"/events/2\", "
                + "\"hits\": 8"
                + "}"
                + "]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .setHeader("Content-Type", "application/json"));

        List<ViewStats> result = statsClient.getStats(statsRequest);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());

        String requestPath = recordedRequest.getPath();
        assertTrue(requestPath.startsWith("/stats?"));
        assertTrue(requestPath.contains("start=2024-01-01+00%3A00%3A00"));
        assertTrue(requestPath.contains("end=2024-01-02+00%3A00%3A00"));
        assertTrue(requestPath.contains("uris=%2Fevents%2F1"));
        assertTrue(requestPath.contains("uris=%2Fevents%2F2"));
        assertTrue(requestPath.contains("unique=true"));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("/events/1", result.get(0).getUri());
        assertEquals(15L, result.get(0).getHits());
    }

    @Test
    void postHit_shouldHandleServerError() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.postHit(hit));

        assertTrue(exception.getMessage().contains("HTTP ошибка: 500"));
    }

    @Test
    void getStats_shouldHandleNotFoundError() {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now())
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.getStats(statsRequest));

        assertTrue(exception.getMessage().contains("HTTP ошибка: 404"));
    }

    @Test
    void getStats_shouldHandleEmptyResponse() throws Exception {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now())
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .setHeader("Content-Type", "application/json"));

        List<ViewStats> result = statsClient.getStats(statsRequest);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void postHit_shouldHandleConnectionError() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        // Останавливаем сервер до отправки запроса
        try {
            mockWebServer.shutdown();
        } catch (Exception e) {
            // Игнорируем ошибки остановки
        }

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.postHit(hit));

        assertTrue(exception.getMessage().contains("IO ошибка"));
    }
}