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
class UpdateCommentAdminRequestSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateCommentAdminRequest updateRequest;

    @BeforeEach
    void setUp() {
        updateRequest = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();
    }

    // Тесты сериализации (объект -> JSON)
    @Test
    void whenSerialize_thenCorrectJson() throws JsonProcessingException {
        // Given
        String expectedJson = "{\"stateAction\":\"APPROVE\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(updateRequest);

        // Then
        assertEquals(expectedJson, actualJson, "Сериализация должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithDifferentStateActions_thenCorrectJson() throws JsonProcessingException {
        // Given
        UpdateCommentAdminRequest rejectRequest = UpdateCommentAdminRequest.builder()
                .stateAction("REJECT")
                .build();

        UpdateCommentAdminRequest publishRequest = UpdateCommentAdminRequest.builder()
                .stateAction("PUBLISH")
                .build();

        // When & Then
        assertEquals("{\"stateAction\":\"REJECT\"}",
                objectMapper.writeValueAsString(rejectRequest),
                "Сериализация для REJECT должна создавать корректный JSON");

        assertEquals("{\"stateAction\":\"PUBLISH\"}",
                objectMapper.writeValueAsString(publishRequest),
                "Сериализация для PUBLISH должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithMaxLengthStateAction_thenCorrectJson() throws JsonProcessingException {
        // Given
        String maxLengthAction = "A".repeat(20);
        UpdateCommentAdminRequest maxLengthRequest = UpdateCommentAdminRequest.builder()
                .stateAction(maxLengthAction)
                .build();

        String expectedJson = "{\"stateAction\":\"" + maxLengthAction + "\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(maxLengthRequest);

        // Then
        assertEquals(expectedJson, actualJson,
                "Сериализация с stateAction максимальной длины должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithSpecialCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String specialStateAction = "STATE_ACTION-123";
        UpdateCommentAdminRequest specialRequest = UpdateCommentAdminRequest.builder()
                .stateAction(specialStateAction)
                .build();

        // When
        String json = objectMapper.writeValueAsString(specialRequest);
        UpdateCommentAdminRequest deserialized = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertEquals(specialStateAction, deserialized.getStateAction(),
                "Сериализация и десериализация должны корректно обрабатывать специальные символы в stateAction");
    }

    // Тесты десериализации (JSON -> объект)
    @Test
    void whenDeserializeValidJson_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"APPROVE\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("APPROVE", result.getStateAction(),
                "stateAction должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithLowerCase_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"approve\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("approve", result.getStateAction(),
                "stateAction в нижнем регистре должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithMixedCase_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"ApPrOvE\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("ApPrOvE", result.getStateAction(),
                "stateAction в смешанном регистре должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithSpaces_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"APPROVE COMMENT\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("APPROVE COMMENT", result.getStateAction(),
                "stateAction с пробелами должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithEmptyString_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("", result.getStateAction(),
                "Пустой stateAction должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithNullStateAction_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":null}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertNull(result.getStateAction(), "Null stateAction должен корректно десериализоваться");
    }

    @Test
    void whenDeserializeWithoutStateActionField_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertNull(result.getStateAction(),
                "stateAction должен быть null при отсутствии поля в JSON");
    }

    @Test
    void whenDeserializeWithExtraFields_thenIgnoreExtraFields() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"REJECT\",\"commentId\":123,\"adminId\":456,\"reason\":\"Spam\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals("REJECT", result.getStateAction(),
                "stateAction должен корректно десериализоваться, лишние поля должны игнорироваться");
    }

    @Test
    void whenDeserializeWithArrayInsteadOfObject_thenThrowException() {
        // Given
        String json = "[{\"stateAction\":\"APPROVE\"}]"; // Массив вместо объекта

        // When & Then
        assertThrows(MismatchedInputException.class, () -> {
            objectMapper.readValue(json, UpdateCommentAdminRequest.class);
        }, "Должно выбрасываться исключение при попытке десериализации массива в объект");
    }

    @Test
    void whenDeserializeMalformedJson_thenThrowException() {
        // Given
        String malformedJson = "{\"stateAction\":\"APPROVE\" // незакрытый комментарий";

        // When & Then
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readValue(malformedJson, UpdateCommentAdminRequest.class);
        }, "Должно выбрасываться исключение при некорректном JSON");
    }

    // Тесты циклической сериализации/десериализации
    @Test
    void whenSerializeThenDeserialize_thenSameObject() throws JsonProcessingException {
        // Given
        UpdateCommentAdminRequest original = UpdateCommentAdminRequest.builder()
                .stateAction("REJECT")
                .build();

        // When
        String json = objectMapper.writeValueAsString(original);
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertEquals(original.getStateAction(), result.getStateAction(),
                "Объект после сериализации и десериализации должен быть эквивалентен исходному");
    }

    @Test
    void whenDeserializeThenSerialize_thenSameJson() throws JsonProcessingException {
        // Given
        String originalJson = "{\"stateAction\":\"PUBLISH_COMMENT\"}";

        // When
        UpdateCommentAdminRequest dto = objectMapper.readValue(originalJson, UpdateCommentAdminRequest.class);
        String resultJson = objectMapper.writeValueAsString(dto);

        // Then
        assertEquals(originalJson, resultJson,
                "JSON после десериализации и сериализации должен быть идентичен исходному");
    }

    // Тесты производительности (опционально)
    @Test
    void whenMultipleSerializations_thenConsistentResults() throws JsonProcessingException {
        // Given
        UpdateCommentAdminRequest dto = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();
        int iterations = 100;

        // When & Then
        for (int i = 0; i < iterations; i++) {
            String json = objectMapper.writeValueAsString(dto);
            UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

            assertEquals(dto.getStateAction(), result.getStateAction(),
                    "Результаты должны быть консистентными при множественных сериализациях/десериализациях");
        }
    }

    // Тесты граничных случаев для stateAction
    @Test
    void whenSerializeUnicodeCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String unicodeStateAction = "APPROVE_КОММЕНТАРИЙ";
        UpdateCommentAdminRequest unicodeDto = UpdateCommentAdminRequest.builder()
                .stateAction(unicodeStateAction)
                .build();

        // When
        String json = objectMapper.writeValueAsString(unicodeDto);
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertEquals(unicodeStateAction, result.getStateAction(),
                "Unicode символы должны корректно сериализоваться и десериализоваться");
    }

    @Test
    void whenSerializeWithEscapedCharacters_thenCorrectJson() throws JsonProcessingException {
        // Given
        String stateActionWithQuotes = "ACTION_WITH_\"QUOTES\"";
        UpdateCommentAdminRequest dto = UpdateCommentAdminRequest.builder()
                .stateAction(stateActionWithQuotes)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertTrue(json.contains("\\\""), "JSON должен содержать экранированные кавычки");

        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);
        assertEquals(stateActionWithQuotes, result.getStateAction(),
                "Экранированные символы должны корректно восстанавливаться");
    }

    // Тесты для builder и конструкторов
    @Test
    void builderShouldCreateObjectWithCorrectValues() {
        // Given
        String expectedStateAction = "CUSTOM_ACTION";

        // When
        UpdateCommentAdminRequest result = UpdateCommentAdminRequest.builder()
                .stateAction(expectedStateAction)
                .build();

        // Then
        assertEquals(expectedStateAction, result.getStateAction(),
                "Builder должен корректно устанавливать stateAction");
    }

    @Test
    void noArgsConstructorShouldCreateObject() {
        // When
        UpdateCommentAdminRequest result = new UpdateCommentAdminRequest();

        // Then
        assertNotNull(result, "Конструктор без аргументов должен создавать объект");
        assertNull(result.getStateAction(),
                "stateAction должен быть null после создания через конструктор без аргументов");
    }

    @Test
    void allArgsConstructorShouldCreateObjectWithCorrectValues() {
        // Given
        String expectedStateAction = "ALL_ARGS_ACTION";

        // When
        UpdateCommentAdminRequest result = new UpdateCommentAdminRequest(expectedStateAction);

        // Then
        assertEquals(expectedStateAction, result.getStateAction(),
                "Конструктор со всеми аргументами должен корректно устанавливать stateAction");
    }

    @Test
    void settersAndGettersShouldWorkCorrectly() {
        // Given
        UpdateCommentAdminRequest dto = new UpdateCommentAdminRequest();
        String expectedStateAction = "SETTER_TEST";

        // When
        dto.setStateAction(expectedStateAction);

        // Then
        assertEquals(expectedStateAction, dto.getStateAction(),
                "Setter и Getter должны корректно работать для stateAction");
    }

    // Тесты для equals и hashCode (генерируемых Lombok)
    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        // Given
        UpdateCommentAdminRequest request1 = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();
        UpdateCommentAdminRequest request2 = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();
        UpdateCommentAdminRequest request3 = UpdateCommentAdminRequest.builder()
                .stateAction("REJECT")
                .build();

        // Then
        assertEquals(request1, request2,
                "Объекты с одинаковым stateAction должны быть равны");
        assertNotEquals(request1, request3,
                "Объекты с разным stateAction не должны быть равны");
        assertEquals(request1.hashCode(), request2.hashCode(),
                "HashCode должен быть одинаковым для равных объектов");
        assertNotEquals(request1.hashCode(), request3.hashCode(),
                "HashCode должен быть разным для разных объектов");
    }

    @Test
    void equalsShouldHandleNull() {
        // Given
        UpdateCommentAdminRequest request = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();

        // Then
        assertNotEquals(null, request, "Объект не должен быть равен null");
    }

    @Test
    void equalsShouldHandleDifferentClass() {
        // Given
        UpdateCommentAdminRequest request = UpdateCommentAdminRequest.builder()
                .stateAction("APPROVE")
                .build();
        Object differentObject = "APPROVE";

        // Then
        assertNotEquals(request, differentObject,
                "Объект не должен быть равен объекту другого класса");
    }
}