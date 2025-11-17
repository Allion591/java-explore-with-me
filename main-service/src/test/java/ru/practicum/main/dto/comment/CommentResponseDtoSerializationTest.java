package ru.practicum.main.dto.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentResponseDtoSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CommentResponseDto commentResponseDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {

        testDateTime = LocalDateTime.of(2023, 12, 15, 14, 30, 45);

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("This is a test comment response")
                .eventId(100L)
                .authorId(50L)
                .created(testDateTime.minusHours(2))
                .updated(testDateTime)
                .state("APPROVED")
                .build();
    }

    // Тесты сериализации (объект -> JSON)
    @Test
    void whenSerialize_thenCorrectJson() throws JsonProcessingException {
        // When
        String json = objectMapper.writeValueAsString(commentResponseDto);

        // Then
        assertNotNull(json, "JSON не должен быть null");
        assertTrue(json.contains("\"id\":1"), "JSON должен содержать id");
        assertTrue(json.contains("\"text\":\"This is a test comment response\""), "JSON должен содержать text");
        assertTrue(json.contains("\"eventId\":100"), "JSON должен содержать eventId");
        assertTrue(json.contains("\"authorId\":50"), "JSON должен содержать authorId");
        assertTrue(json.contains("\"state\":\"APPROVED\""), "JSON должен содержать state");
        assertTrue(json.contains("\"created\""), "JSON должен содержать created поле");
        assertTrue(json.contains("\"updated\""), "JSON должен содержать updated поле");
    }

    @Test
    void whenSerializeWithNullFields_thenCorrectJson() throws JsonProcessingException {
        // Given
        CommentResponseDto dtoWithNulls = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(null)
                .updated(null)
                .state(null)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dtoWithNulls);

        // Then
        assertNotNull(json, "JSON не должен быть null");
        assertTrue(json.contains("\"created\":null"), "JSON должен содержать null для created");
        assertTrue(json.contains("\"updated\":null"), "JSON должен содержать null для updated");
        assertTrue(json.contains("\"state\":null"), "JSON должен содержать null для state");
    }

    // Тесты десериализации (JSON -> объект)
    @Test
    void whenDeserializeValidJson_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"text\":\"Test comment\"," +
                "\"eventId\":100," +
                "\"authorId\":50," +
                "\"created\":\"2023-12-15T12:30:45\"," +
                "\"updated\":\"2023-12-15T14:30:45\"," +
                "\"state\":\"APPROVED\"" +
                "}";

        // When
        CommentResponseDto result = objectMapper.readValue(json, CommentResponseDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getText());
        assertEquals(100L, result.getEventId());
        assertEquals(50L, result.getAuthorId());
        assertEquals(LocalDateTime.of(2023, 12, 15, 12, 30, 45), result.getCreated());
        assertEquals(LocalDateTime.of(2023, 12, 15, 14, 30, 45), result.getUpdated());
        assertEquals("APPROVED", result.getState());
    }

    @Test
    void whenDeserializeWithDifferentDateTimeFormats_thenCorrectObject() throws JsonProcessingException {
        // Given - Jackson обычно принимает ISO format
        String json = "{" +
                "\"id\":1," +
                "\"text\":\"Test\"," +
                "\"eventId\":100," +
                "\"authorId\":50," +
                "\"created\":\"2023-12-15T12:30:45.123\"," + // с миллисекундами
                "\"updated\":\"2023-12-15T14:30:45\"," +
                "\"state\":\"PENDING\"" +
                "}";

        // When
        CommentResponseDto result = objectMapper.readValue(json, CommentResponseDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals(LocalDateTime.of(2023, 12, 15, 12, 30, 45, 123000000), result.getCreated());
    }

    @Test
    void whenDeserializeWithMissingOptionalFields_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"text\":\"Test comment\"," +
                "\"eventId\":100," +
                "\"authorId\":50" +
                "}";

        // When
        CommentResponseDto result = objectMapper.readValue(json, CommentResponseDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getText());
        assertEquals(100L, result.getEventId());
        assertEquals(50L, result.getAuthorId());
        assertNull(result.getCreated());
        assertNull(result.getUpdated());
        assertNull(result.getState());
    }

    @Test
    void whenDeserializeWithNullFields_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"text\":\"Test comment\"," +
                "\"eventId\":100," +
                "\"authorId\":50," +
                "\"created\":null," +
                "\"updated\":null," +
                "\"state\":null" +
                "}";

        // When
        CommentResponseDto result = objectMapper.readValue(json, CommentResponseDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertNull(result.getCreated());
        assertNull(result.getUpdated());
        assertNull(result.getState());
    }

    // Тесты обработки ошибок десериализации
    @Test
    void whenDeserializeWithInvalidDateTimeFormat_thenThrowException() {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"text\":\"Test\"," +
                "\"eventId\":100," +
                "\"authorId\":50," +
                "\"created\":\"2023/12/15 12:30:45\"," + // Неправильный формат
                "\"state\":\"APPROVED\"" +
                "}";

        // When & Then
        assertThrows(InvalidFormatException.class, () -> {
            objectMapper.readValue(json, CommentResponseDto.class);
        }, "Должно выбрасываться исключение при неправильном формате даты");
    }

    @Test
    void whenDeserializeWithInvalidFieldType_thenThrowException() {
        // Given
        String json = "{" +
                "\"id\":\"not-a-number\"," + // Строка вместо числа
                "\"text\":\"Test\"," +
                "\"eventId\":100," +
                "\"authorId\":50" +
                "}";

        // When & Then
        assertThrows(InvalidFormatException.class, () -> {
            objectMapper.readValue(json, CommentResponseDto.class);
        }, "Должно выбрасываться исключение при несовпадении типов полей");
    }

    // Тесты циклической сериализации/десериализации
    @Test
    void whenSerializeThenDeserialize_thenSameObject() throws JsonProcessingException {
        // Given
        CommentResponseDto original = CommentResponseDto.builder()
                .id(1L)
                .text("Round trip test")
                .eventId(100L)
                .authorId(50L)
                .created(testDateTime.minusDays(1))
                .updated(testDateTime)
                .state("APPROVED")
                .build();

        // When
        String json = objectMapper.writeValueAsString(original);
        CommentResponseDto result = objectMapper.readValue(json, CommentResponseDto.class);

        // Then
        assertEquals(original.getId(), result.getId(), "ID должен сохраняться");
        assertEquals(original.getText(), result.getText(), "Text должен сохраняться");
        assertEquals(original.getEventId(), result.getEventId(), "EventId должен сохраняться");
        assertEquals(original.getAuthorId(), result.getAuthorId(), "AuthorId должен сохраняться");
        assertEquals(original.getCreated(), result.getCreated(), "Created дата должна сохраняться");
        assertEquals(original.getUpdated(), result.getUpdated(), "Updated дата должна сохраняться");
        assertEquals(original.getState(), result.getState(), "State должен сохраняться");
    }

    @Test
    void whenDeserializeThenSerialize_thenSameJsonStructure() throws JsonProcessingException {
        // Given
        String originalJson = "{" +
                "\"id\":1," +
                "\"text\":\"Test comment for roundtrip\"," +
                "\"eventId\":100," +
                "\"authorId\":50," +
                "\"created\":\"2023-12-15T10:30:45\"," +
                "\"updated\":\"2023-12-15T14:30:45\"," +
                "\"state\":\"REJECTED\"" +
                "}";

        // When
        CommentResponseDto dto = objectMapper.readValue(originalJson, CommentResponseDto.class);
        String resultJson = objectMapper.writeValueAsString(dto);

        // Then - проверяем, что все поля присутствуют и значения корректны
        CommentResponseDto roundTripDto = objectMapper.readValue(resultJson, CommentResponseDto.class);

        assertEquals(1L, roundTripDto.getId());
        assertEquals("Test comment for roundtrip", roundTripDto.getText());
        assertEquals(100L, roundTripDto.getEventId());
        assertEquals(50L, roundTripDto.getAuthorId());
        assertEquals(LocalDateTime.of(2023, 12, 15, 10, 30, 45), roundTripDto.getCreated());
        assertEquals(LocalDateTime.of(2023, 12, 15, 14, 30, 45), roundTripDto.getUpdated());
        assertEquals("REJECTED", roundTripDto.getState());
    }

    // Тесты для проверки формата дат
    @Test
    void whenSerializeLocalDateTime_thenIsoFormat() throws JsonProcessingException {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 15, 14, 30, 45, 123456789);
        CommentResponseDto dto = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(dateTime)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        // Jackson обычно сериализует LocalDateTime в ISO format (возможно с наносекундами)
        assertTrue(json.contains("\"2023-12-15T14:30:45"), "Дата должна быть в ISO формате");
    }

    // Тесты для builder
    @Test
    void builderShouldCreateCompleteObject() {
        // When
        CommentResponseDto dto = CommentResponseDto.builder()
                .id(1L)
                .text("Builder test")
                .eventId(100L)
                .authorId(50L)
                .created(testDateTime.minusHours(1))
                .updated(testDateTime)
                .state("PENDING")
                .build();

        // Then
        assertNotNull(dto, "Объект не должен быть null");
        assertEquals(1L, dto.getId());
        assertEquals("Builder test", dto.getText());
        assertEquals(100L, dto.getEventId());
        assertEquals(50L, dto.getAuthorId());
        assertEquals(testDateTime.minusHours(1), dto.getCreated());
        assertEquals(testDateTime, dto.getUpdated());
        assertEquals("PENDING", dto.getState());
    }

    @Test
    void builderWithPartialFieldsShouldWork() {
        // When
        CommentResponseDto dto = CommentResponseDto.builder()
                .id(1L)
                .text("Partial")
                .eventId(100L)
                .authorId(50L)
                .build();

        // Then
        assertNotNull(dto, "Объект не должен быть null");
        assertEquals(1L, dto.getId());
        assertEquals("Partial", dto.getText());
        assertEquals(100L, dto.getEventId());
        assertEquals(50L, dto.getAuthorId());
        assertNull(dto.getCreated());
        assertNull(dto.getUpdated());
        assertNull(dto.getState());
    }

    // Тесты для equals/hashCode (генерируемых Lombok)
    @Test
    void equalsAndHashCodeShouldWorkWithAllFields() {
        // Given
        CommentResponseDto dto1 = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(testDateTime)
                .updated(testDateTime)
                .state("APPROVED")
                .build();

        CommentResponseDto dto2 = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(testDateTime)
                .updated(testDateTime)
                .state("APPROVED")
                .build();

        // Then
        assertEquals(dto1, dto2, "Объекты с одинаковыми полями должны быть равны");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "HashCode должен быть одинаковым для равных объектов");
    }

    @Test
    void equalsAndHashCodeShouldWorkWithNullFields() {
        // Given
        CommentResponseDto dto1 = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(null)
                .updated(null)
                .state(null)
                .build();

        CommentResponseDto dto2 = CommentResponseDto.builder()
                .id(1L)
                .text("Test")
                .eventId(100L)
                .authorId(50L)
                .created(null)
                .updated(null)
                .state(null)
                .build();

        // Then
        assertEquals(dto1, dto2, "Объекты с null полями должны быть равны");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "HashCode должен быть одинаковым для объектов с null полями");
    }
}