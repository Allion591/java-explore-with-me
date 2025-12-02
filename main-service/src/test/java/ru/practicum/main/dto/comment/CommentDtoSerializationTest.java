package ru.practicum.main.dto.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentDtoSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        commentDto = CommentDto.builder()
                .text("This is a test comment")
                .build();
    }

    // Тесты сериализации (объект -> JSON)
    @Test
    void whenSerialize_thenCorrectJson() throws JsonProcessingException {
        // Given
        String expectedJson = "{\"text\":\"This is a test comment\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(commentDto);

        // Then
        assertEquals(expectedJson, actualJson, "Сериализация должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithEmptyText_thenCorrectJson() throws JsonProcessingException {
        // Given
        CommentDto emptyTextDto = CommentDto.builder().text("").build();
        String expectedJson = "{\"text\":\"\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(emptyTextDto);

        // Then
        assertEquals(expectedJson, actualJson, "Сериализация с пустым текстом должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithMaxLengthText_thenCorrectJson() throws JsonProcessingException {
        // Given
        String maxLengthText = "a".repeat(255);
        CommentDto maxLengthDto = CommentDto.builder().text(maxLengthText).build();
        String expectedJson = "{\"text\":\"" + maxLengthText + "\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(maxLengthDto);

        // Then
        assertEquals(expectedJson, actualJson, "Сериализация с текстом максимальной длины должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithSpecialCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String textWithSpecialChars = "Comment with \"quotes\", \nnewline, \ttab, and emoji 😀";
        CommentDto specialCharsDto = CommentDto.builder().text(textWithSpecialChars).build();

        // When
        String json = objectMapper.writeValueAsString(specialCharsDto);
        CommentDto deserializedDto = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertEquals(textWithSpecialChars, deserializedDto.getText(),
                "Сериализация и десериализация должны корректно обрабатывать специальные символы");
    }

    // Тесты десериализации (JSON -> объект)
    @Test
    void whenDeserializeValidJson_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"text\":\"This is a test comment\"}";

        // When
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("This is a test comment", result.getText(),
                "Текст должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithEmptyText_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"text\":\"\"}";

        // When
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("", result.getText(), "Пустой текст должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithNullText_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"text\":null}";

        // When
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertNull(result.getText(), "Null текст должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithoutTextField_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{}";

        // When
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertNull(result.getText(), "Текст должен быть null при отсутствии поля в JSON");
    }

    @Test
    void whenDeserializeWithExtraFields_thenIgnoreExtraFields() throws JsonProcessingException {
        // Given
        String json = "{\"text\":\"Test comment\",\"id\":123,\"author\":\"John\",\"createdDate\":\"2023-01-01\"}";

        // When
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("Test comment", result.getText(),
                "Текст должен корректно десериализоваться, лишние поля должны игнорироваться");
    }

    @Test
    void whenDeserializeWithArrayInsteadOfObject_thenThrowException() {
        // Given
        String json = "[{\"text\":\"test\"}]"; // Массив вместо объекта

        // When & Then
        assertThrows(MismatchedInputException.class, () -> {
            objectMapper.readValue(json, CommentDto.class);
        }, "Должно выбрасываться исключение при попытке десериализации массива в объект");
    }

    @Test
    void whenDeserializeMalformedJson_thenThrowException() {
        // Given
        String malformedJson = "{\"text\":\"test\" // незакрытый комментарий";

        // When & Then
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readValue(malformedJson, CommentDto.class);
        }, "Должно выбрасываться исключение при некорректном JSON");
    }

    // Тесты циклической сериализации/десериализации
    @Test
    void whenSerializeThenDeserialize_thenSameObject() throws JsonProcessingException {
        // Given
        CommentDto original = CommentDto.builder()
                .text("Original comment text")
                .build();

        // When
        String json = objectMapper.writeValueAsString(original);
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertEquals(original.getText(), result.getText(),
                "Объект после сериализации и десериализации должен быть эквивалентен исходному");
    }

    @Test
    void whenDeserializeThenSerialize_thenSameJson() throws JsonProcessingException {
        // Given
        String originalJson = "{\"text\":\"Test comment for roundtrip\"}";

        // When
        CommentDto dto = objectMapper.readValue(originalJson, CommentDto.class);
        String resultJson = objectMapper.writeValueAsString(dto);

        // Then
        assertEquals(originalJson, resultJson,
                "JSON после десериализации и сериализации должен быть идентичен исходному");
    }

    // Тесты производительности (опционально)
    @Test
    void whenMultipleSerializations_thenConsistentResults() throws JsonProcessingException {
        // Given
        CommentDto dto = CommentDto.builder().text("Performance test").build();
        int iterations = 100;

        // When & Then
        for (int i = 0; i < iterations; i++) {
            String json = objectMapper.writeValueAsString(dto);
            CommentDto result = objectMapper.readValue(json, CommentDto.class);

            assertEquals(dto.getText(), result.getText(),
                    "Результаты должны быть консистентными при множественных сериализациях/десериализациях");
        }
    }

    // Тесты граничных случаев
    @Test
    void whenSerializeUnicodeCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String unicodeText = "Комментарий с русскими символами и emoji 🚀";
        CommentDto unicodeDto = CommentDto.builder().text(unicodeText).build();

        // When
        String json = objectMapper.writeValueAsString(unicodeDto);
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        // Then
        assertEquals(unicodeText, result.getText(),
                "Unicode символы должны корректно сериализоваться и десериализоваться");
    }

    @Test
    void whenSerializeWithEscapedCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String textWithQuotes = "Comment with \"quoted\" text";
        CommentDto dto = CommentDto.builder().text(textWithQuotes).build();

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertTrue(json.contains("\\\""), "JSON должен содержать экранированные кавычки");

        CommentDto result = objectMapper.readValue(json, CommentDto.class);
        assertEquals(textWithQuotes, result.getText(),
                "Экранированные символы должны корректно восстанавливаться");
    }
}