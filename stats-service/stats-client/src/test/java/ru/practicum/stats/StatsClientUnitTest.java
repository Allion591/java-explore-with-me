package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.StatsClient.StatsClient;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.StatsRequest;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsClientException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientUnitTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestCaptor;

    private StatsClient statsClient;
    private ObjectMapper objectMapper;
    private final String baseUrl = "http://stats-server:9090";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Регистрируем модуль для поддержки Java 8 дат/времени
        objectMapper.registerModule(new JavaTimeModule());

        // Создаем StatsClient с настроенным ObjectMapper
        statsClient = new StatsClient(objectMapper, baseUrl);

        // Используем рефлексию для замены HttpClient на мок
        replaceHttpClientWithMock();
    }

    private void replaceHttpClientWithMock() {
        try {
            var field = StatsClient.class.getDeclaredField("httpClient");
            field.setAccessible(true);
            field.set(statsClient, httpClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock HttpClient", e);
        }
    }

    @Test
    void postHit_shouldSendCorrectRequest() throws Exception {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setIp("192.168.1.1");
        hit.setUri("/events/1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        String responseBody = objectMapper.writeValueAsString(hit);

        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        EndpointHit result = statsClient.postHit(hit);

        verify(httpClient).send(httpRequestCaptor.capture(), any());
        HttpRequest capturedRequest = httpRequestCaptor.getValue();

        assertEquals("POST", capturedRequest.method());
        assertEquals(baseUrl + "/hit", capturedRequest.uri().toString());
        assertTrue(capturedRequest.headers().firstValue("Content-Type").get().contains("application/json"));

        assertEquals(hit.getApp(), result.getApp());
        assertEquals(hit.getUri(), result.getUri());
    }

    @Test
    void postHit_shouldThrowStatsClientExceptionOnHttpError() throws Exception {
        EndpointHit hit = new EndpointHit();
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("Bad Request");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.postHit(hit));

        assertTrue(exception.getMessage().contains("HTTP ошибка: 400"));
    }

    @Test
    void postHit_shouldHandleIOException() throws Exception {

        EndpointHit hit = new EndpointHit();
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.postHit(hit));

        assertTrue(exception.getMessage().contains("IO ошибка"));
    }

    @Test
    void postHit_shouldHandleInterruptedException() throws Exception {
        EndpointHit hit = new EndpointHit();
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Request interrupted"));

        StatsClientException exception = assertThrows(StatsClientException.class,
                () -> statsClient.postHit(hit));

        assertTrue(exception.getMessage().contains("Запрос прерван"));
        // Проверяем, что флаг прерывания восстановлен
        assertTrue(Thread.interrupted());
    }

    @Test
    void getStats_shouldBuildCorrectUri() throws Exception {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.of(2024, 1, 1, 0, 0))
                .end(LocalDateTime.of(2024, 1, 2, 0, 0))
                .uris(List.of("/events/1", "/events/2"))
                .unique(true)
                .build();

        List<ViewStats> viewStats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 10L),
                new ViewStats("ewm-main-service", "/events/2", 5L)
        );
        String responseBody = objectMapper.writeValueAsString(viewStats);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        List<ViewStats> result = statsClient.getStats(statsRequest);

        verify(httpClient).send(httpRequestCaptor.capture(), any());
        HttpRequest capturedRequest = httpRequestCaptor.getValue();

        String uri = capturedRequest.uri().toString();
        assertTrue(uri.startsWith(baseUrl + "/stats?"));
        assertTrue(uri.contains("start=2024-01-01+00%3A00%3A00"));
        assertTrue(uri.contains("end=2024-01-02+00%3A00%3A00"));
        assertTrue(uri.contains("uris=%2Fevents%2F1"));
        assertTrue(uri.contains("uris=%2Fevents%2F2"));
        assertTrue(uri.contains("unique=true"));

        assertEquals(2, result.size());
        assertEquals("/events/1", result.get(0).getUri());
    }

    @Test
    void getStats_shouldBuildUriWithoutOptionalParams() throws Exception {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.of(2024, 1, 1, 0, 0))
                .end(LocalDateTime.of(2024, 1, 2, 0, 0))
                .build();

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("[]");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        statsClient.getStats(statsRequest);

        verify(httpClient).send(httpRequestCaptor.capture(), any());
        HttpRequest capturedRequest = httpRequestCaptor.getValue();

        String uri = capturedRequest.uri().toString();
        assertTrue(uri.contains("start=2024-01-01+00%3A00%3A00"));
        assertTrue(uri.contains("end=2024-01-02+00%3A00%3A00"));
        assertFalse(uri.contains("uris="));
        assertTrue(uri.contains("unique=false"));
    }

    @Test
    void getStats_shouldHandleJsonProcessingException() throws Exception {
        StatsRequest statsRequest = StatsRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("invalid json");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        assertThrows(StatsClientException.class, () -> statsClient.getStats(statsRequest));
    }
}