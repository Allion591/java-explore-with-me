package ru.practicum.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EndpointHitTest {

    private static Validator validator;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateValidEndpointHit() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailValidationWhenAppIsBlank() {
        EndpointHit hit = new EndpointHit();
        hit.setApp(""); // Пустое значение
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("Название микросервиса не может быть пустым", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenAppIsTooLong() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("a".repeat(256)); // 256 символов - больше лимита
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("Имя приложения не может быть длиннее 255 символов", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenUriIsBlank() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri(""); // Пустой URI
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("URI запроса не может быть пустым", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenUriIsTooLong() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/" + "a".repeat(500)); // 501 символ - больше лимита
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("URI не может быть длиннее 500 символов", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenIpIsBlank() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("");
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(2, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("Неверный формат ip адреса", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenIpIsInvalid() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("invalid-ip-address"); // Невалидный IP
        hit.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("Неверный формат ip адреса", violation.getMessage());
    }

    @Test
    void shouldAcceptValidIpFormats() {
        String[] validIps = {"192.168.1.1", "::1", "localhost", "2001:db8::1"};

        for (String ip : validIps) {
            EndpointHit hit = new EndpointHit();
            hit.setApp("ewm-main-service");
            hit.setUri("/events/1");
            hit.setIp(ip);
            hit.setTimestamp(LocalDateTime.now());

            Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

            assertTrue(violations.isEmpty(), "IP " + ip + " должен быть валидным");
        }
    }

    @Test
    void shouldFailValidationWhenTimestampIsNull() {

        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertEquals("Дата и время запроса не указано", violation.getMessage());
    }

    @Test
    void shouldSerializeToJsonWithCorrectFormat() throws JsonProcessingException {
        EndpointHit hit = new EndpointHit();
        hit.setId(1L);
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 30, 45));

        String json = objectMapper.writeValueAsString(hit);

        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
        assertTrue(json.contains("\"uri\":\"/events/1\""));
        assertTrue(json.contains("\"ip\":\"192.168.1.1\""));
        assertTrue(json.contains("\"timestamp\":\"2024-01-01 10:30:45\""));
    }

    @Test
    void shouldDeserializeFromJsonWithCorrectFormat() throws JsonProcessingException {
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
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30, 45), hit.getTimestamp());
    }

    @Test
    void shouldHandleEqualsAndHashCode() {
        EndpointHit hit1 = new EndpointHit();
        hit1.setId(1L);
        hit1.setApp("app1");
        hit1.setUri("/uri1");
        hit1.setIp("192.168.1.1");
        hit1.setTimestamp(LocalDateTime.now());
        EndpointHit hit2 = new EndpointHit();
        hit2.setId(1L);
        hit2.setApp("app1");
        hit2.setUri("/uri1");
        hit2.setIp("192.168.1.1");
        hit2.setTimestamp(hit1.getTimestamp());

        EndpointHit hit3 = new EndpointHit();
        hit3.setId(2L);
        hit3.setApp("app2");
        hit3.setUri("/uri2");
        hit3.setIp("192.168.1.2");
        hit3.setTimestamp(LocalDateTime.now().plusHours(1));

        assertEquals(hit1, hit2, "Объекты с одинаковыми полями должны быть равны");
        assertNotEquals(hit1, hit3, "Объекты с разными полями не должны быть равны");
        assertEquals(hit1.hashCode(), hit2.hashCode(), "HashCode должен быть одинаковым для равных объектов");
    }

    @Test
    void shouldImplementToString() {
        EndpointHit hit = new EndpointHit();
        hit.setId(1L);
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        String toString = hit.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ewm-main-service"));
        assertTrue(toString.contains("/events/1"));
    }
}