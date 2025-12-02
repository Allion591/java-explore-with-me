package ru.practicum.main.dto.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.enums.CommentStatus;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UpdateCommentAdminRequestSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateCommentAdminRequest updateRequest;

    @BeforeEach
    void setUp() {
        updateRequest = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.APPROVED)
                .build();
    }

    // Тесты сериализации (объект -> JSON)
    @Test
    void whenSerialize_thenCorrectJson() throws JsonProcessingException {
        // Given
        String expectedJson = "{\"stateAction\":\"APPROVED\"}";

        // When
        String actualJson = objectMapper.writeValueAsString(updateRequest);

        // Then
        assertEquals(expectedJson, actualJson, "Сериализация должна создавать корректный JSON");
    }

    @Test
    void whenSerializeWithDifferentStateActions_thenCorrectJson() throws JsonProcessingException {
        // Given
        UpdateCommentAdminRequest rejectRequest = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.REJECTED)
                .build();

        UpdateCommentAdminRequest publishRequest = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.APPROVED)
                .build();

        // When & Then
        assertEquals("{\"stateAction\":\"REJECTED\"}",
                objectMapper.writeValueAsString(rejectRequest),
                "Сериализация для REJECT должна создавать корректный JSON");

        assertEquals("{\"stateAction\":\"APPROVED\"}",
                objectMapper.writeValueAsString(publishRequest),
                "Сериализация для PUBLISH должна создавать корректный JSON");
    }

    // Тесты десериализации (JSON -> объект)
    @Test
    void whenDeserializeValidJson_thenCorrectObject() throws JsonProcessingException {
        // Given
        String json = "{\"stateAction\":\"APPROVED\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals(CommentStatus.APPROVED, result.getStateAction(),
                "stateAction должен корректно десериализоваться");
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
        String json = "{\"stateAction\":\"REJECTED\",\"commentId\":123,\"adminId\":456,\"reason\":\"Spam\"}";

        // When
        UpdateCommentAdminRequest result = objectMapper.readValue(json, UpdateCommentAdminRequest.class);

        // Then
        assertNotNull(result, "Десериализованный объект не должен быть null");
        assertEquals(CommentStatus.REJECTED, result.getStateAction(),
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
                .stateAction(CommentStatus.REJECTED)
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
        String originalJson = "{\"stateAction\":\"APPROVED\"}";

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
                .stateAction(CommentStatus.APPROVED)
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

    @Test
    void noArgsConstructorShouldCreateObject() {
        // When
        UpdateCommentAdminRequest result = new UpdateCommentAdminRequest();

        // Then
        assertNotNull(result, "Конструктор без аргументов должен создавать объект");
        assertNull(result.getStateAction(),
                "stateAction должен быть null после создания через конструктор без аргументов");
    }

    // Тесты для equals и hashCode (генерируемых Lombok)
    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        // Given
        UpdateCommentAdminRequest request1 = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.APPROVED)
                .build();
        UpdateCommentAdminRequest request2 = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.APPROVED)
                .build();
        UpdateCommentAdminRequest request3 = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.REJECTED)
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
                .stateAction(CommentStatus.APPROVED)
                .build();

        // Then
        assertNotEquals(null, request, "Объект не должен быть равен null");
    }

    @Test
    void equalsShouldHandleDifferentClass() {
        // Given
        UpdateCommentAdminRequest request = UpdateCommentAdminRequest.builder()
                .stateAction(CommentStatus.APPROVED)
                .build();
        Object differentObject = "APPROVED";

        // Then
        assertNotEquals(request, differentObject,
                "Объект не должен быть равен объекту другого класса");
    }
}