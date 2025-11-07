package ru.practicum.main.dto.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewCategoryDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void newCategoryDto_ShouldSerializeToJson() throws JsonProcessingException {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        String json = objectMapper.writeValueAsString(newCategoryDto);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Концерты\""));
        assertFalse(json.contains("\"id\"")); // Не должно быть id, так как его нет в DTO
    }

    @Test
    void newCategoryDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        String json = "{\"name\":\"Концерты\"}";

        NewCategoryDto newCategoryDto = objectMapper.readValue(json, NewCategoryDto.class);

        assertNotNull(newCategoryDto);
        assertEquals("Концерты", newCategoryDto.getName());
    }

    @Test
    void newCategoryDto_ShouldSerializeAndDeserialize_WithSpecialCharacters() throws JsonProcessingException {
        NewCategoryDto originalDto = NewCategoryDto.builder()
                .name("Концерты, фестивали - ивенты!")
                .build();

        String json = objectMapper.writeValueAsString(originalDto);
        NewCategoryDto deserializedDto = objectMapper.readValue(json, NewCategoryDto.class);

        assertNotNull(json);
        assertEquals(originalDto.getName(), deserializedDto.getName());
    }

    @Test
    void newCategoryDto_ShouldIgnoreUnknownProperties() throws JsonProcessingException {
        String json = "{\"name\":\"Концерты\",\"id\":1,\"unknownField\":\"value\"}";

        NewCategoryDto newCategoryDto = objectMapper.readValue(json, NewCategoryDto.class);

        assertNotNull(newCategoryDto);
        assertEquals("Концерты", newCategoryDto.getName());
    }

    @Test
    void newCategoryDto_ShouldHandleEmptyObject() throws JsonProcessingException {
        String json = "{}";

        NewCategoryDto newCategoryDto = objectMapper.readValue(json, NewCategoryDto.class);

        assertNotNull(newCategoryDto);
        assertNull(newCategoryDto.getName());
    }
}