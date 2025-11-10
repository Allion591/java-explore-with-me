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
import ru.practicum.stats.statsClient.StatsClient;
import ru.practicum.stats.dto.ViewStats;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final String appName = "ewm-main-service";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Создаем StatsClient с настроенным ObjectMapper и appName
        statsClient = new StatsClient(baseUrl, appName);

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
    void getEventsViews_shouldReturnCorrectMap() throws Exception {
        Set<Long> eventIds = Set.of(1L, 2L);
        boolean unique = true;

        List<ViewStats> viewStats = List.of(
                new ViewStats(appName, "/events/1", 10L),
                new ViewStats(appName, "/events/2", 5L)
        );
        String responseBody = objectMapper.writeValueAsString(viewStats);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, unique);

        verify(httpClient).send(httpRequestCaptor.capture(), any());
        HttpRequest capturedRequest = httpRequestCaptor.getValue();

        String requestUri = capturedRequest.uri().toString();
        assertTrue(requestUri.startsWith(baseUrl + "/stats?"));
        assertTrue(requestUri.contains("uris=%2Fevents%2F1"));
        assertTrue(requestUri.contains("uris=%2Fevents%2F2"));
        assertTrue(requestUri.contains("unique=true"));

        assertEquals(2, result.size());
        assertEquals(10L, result.get(1L));
        assertEquals(5L, result.get(2L));
    }

    @Test
    void getEventsViews_withEmptyEventIds_shouldReturnEmptyMap() throws IOException, InterruptedException {
        Set<Long> eventIds = Set.of();
        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
        verify(httpClient, never()).send(any(HttpRequest.class), any());
    }

    @Test
    void getEventViews_shouldReturnCorrectViews() throws Exception {
        Long eventId = 1L;
        boolean unique = true;

        List<ViewStats> viewStats = List.of(
                new ViewStats(appName, "/events/1", 15L)
        );
        String responseBody = objectMapper.writeValueAsString(viewStats);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        Long result = statsClient.getEventViews(eventId, unique);

        assertEquals(15L, result);
    }

    @Test
    void getEventViews_whenEventNotFound_shouldReturnZero() throws Exception {
        Long eventId = 999L;
        boolean unique = true;

        List<ViewStats> viewStats = List.of(); // Пустой список - событие не найдено
        String responseBody = objectMapper.writeValueAsString(viewStats);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        Long result = statsClient.getEventViews(eventId, unique);

        assertEquals(0L, result);
    }

    @Test
    void getEventsViews_shouldHandleHttpError() throws Exception {
        Set<Long> eventIds = Set.of(1L, 2L);

        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("Bad Request");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        // Должен вернуть пустую map при ошибке
        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsViews_shouldHandleIOException() throws Exception {
        Set<Long> eventIds = Set.of(1L);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsViews_shouldHandleInterruptedException() throws Exception {
        Set<Long> eventIds = Set.of(1L);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Request interrupted"));

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
        assertTrue(Thread.interrupted()); // Проверяем, что флаг прерывания восстановлен
    }

    @Test
    void extractEventIdFromUri_shouldWorkCorrectly() throws Exception {
        // Используем рефлексию для тестирования приватного метода
        var method = StatsClient.class.getDeclaredMethod("extractEventIdFromUri", String.class);
        method.setAccessible(true);

        assertEquals(1L, method.invoke(statsClient, "/events/1"));
        assertEquals(123L, method.invoke(statsClient, "/events/123"));
        assertEquals(-1L, method.invoke(statsClient, "invalid-uri"));
        assertEquals(-1L, method.invoke(statsClient, "/events/not-a-number"));
    }

    @Test
    void getEventsViews_withUniqueFalse_shouldBuildCorrectUri() throws Exception {
        Set<Long> eventIds = Set.of(1L);
        boolean unique = false;

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("[]");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        statsClient.getEventsViews(eventIds, unique);

        verify(httpClient).send(httpRequestCaptor.capture(), any());
        HttpRequest capturedRequest = httpRequestCaptor.getValue();

        String uri = capturedRequest.uri().toString();
        assertTrue(uri.contains("unique=false"));
    }

    @Test
    void getEventsViews_shouldHandleMalformedJson() throws Exception {
        Set<Long> eventIds = Set.of(1L);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("invalid json");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        Map<Long, Long> result = statsClient.getEventsViews(eventIds, true);

        assertTrue(result.isEmpty());
    }
}