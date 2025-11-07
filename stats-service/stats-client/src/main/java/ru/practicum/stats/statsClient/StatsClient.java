package ru.practicum.stats.statsClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.StatsRequest;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsClientException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url}") String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;

        // Настраиваем ObjectMapper для правильной работы с LocalDateTime
        this.objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Регистрием сериализатор и десериализатор для LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(formatter));

        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public EndpointHit postHit(EndpointHit hit) throws StatsClientException {
        log.info("Клиент принял запрос на отправку в сервис: ip:{}, app:{}", hit.getIp(), hit.getApp());
        try {
            String requestBody = objectMapper.writeValueAsString(hit);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/hit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.debug("Успешно получен ответ, response: {}", response.body());
                return objectMapper.readValue(response.body(), EndpointHit.class);
            } else {
                throw new StatsClientException("HTTP ошибка: " + response.statusCode() + " - " + response.body());
            }

        } catch (IOException e) {
            log.error("Ошибка отправки запроса IO: {}", e.getMessage());
            throw new StatsClientException("IO ошибка: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Ошибка отправки запроса InterruptedException: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new StatsClientException("Запрос прерван: " + e.getMessage(), e);
        }
    }

    public List<ViewStats> getStats(StatsRequest statsRequest) throws StatsClientException {
        log.info("Клиент принял запрос вывод статистики: запрос: {}", statsRequest);
        try {
            URI uri = buildStatsUri(statsRequest);
            log.debug("Сформированный URI: {}", uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                log.debug("Успешно получен ответ от сервиса, response: {}", response.body());
                return objectMapper.readValue(response.body(), new TypeReference<List<ViewStats>>() {
                });
            } else {
                throw new StatsClientException("HTTP ошибка: " + response.statusCode() + " - " + response.body());
            }

        } catch (IOException e) {
            log.error("Ошибка отправки GET запроса IO: {}", e.getMessage());
            throw new StatsClientException("IO ошибка: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Ошибка отправки GET запроса InterruptedException: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new StatsClientException("Запрос прерван: " + e.getMessage(), e);
        }
    }

    private URI buildStatsUri(StatsRequest statsRequest) {
        log.debug("Формируем строку запроса: {}", statsRequest);

        List<String> params = new ArrayList<>();

        params.add("start=" + encodeValue(statsRequest.getStart().format(formatter)));
        params.add("end=" + encodeValue(statsRequest.getEnd().format(formatter)));

        if (statsRequest.getUris() != null && !statsRequest.getUris().isEmpty()) {
            statsRequest.getUris().forEach(uri ->
                    params.add("uris=" + encodeValue(uri))
            );
        }

        boolean unique = statsRequest.getUnique() != null ? statsRequest.getUnique() : false;
        params.add("unique=" + unique);

        String queryString = String.join("&", params);
        return URI.create(baseUrl + "/stats?" + queryString);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}