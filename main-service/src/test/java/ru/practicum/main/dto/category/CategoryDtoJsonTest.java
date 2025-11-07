package ru.practicum.main.dto.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void categoryDto_ShouldSerializeToJson() throws JsonProcessingException {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        String json = objectMapper.writeValueAsString(categoryDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Концерты\""));
    }

    @Test
    void categoryDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"Концерты\"}";

        CategoryDto categoryDto = objectMapper.readValue(json, CategoryDto.class);

        assertNotNull(categoryDto);
        assertEquals(1L, categoryDto.getId());
        assertEquals("Концерты", categoryDto.getName());
    }

    @Test
    void categoryDto_ShouldSerializeAndDeserialize_WithSpecialCharacters() throws JsonProcessingException {
        CategoryDto originalDto = CategoryDto.builder()
                .id(2L)
                .name("Концерты, фестивали - ивенты!")
                .build();

        String json = objectMapper.writeValueAsString(originalDto);
        CategoryDto deserializedDto = objectMapper.readValue(json, CategoryDto.class);

        assertNotNull(json);
        assertEquals(originalDto.getId(), deserializedDto.getId());
        assertEquals(originalDto.getName(), deserializedDto.getName());
    }

    @Test
    void categoryDto_ShouldHandleNullValues() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":null}";

        CategoryDto categoryDto = objectMapper.readValue(json, CategoryDto.class);

        assertNotNull(categoryDto);
        assertEquals(1L, categoryDto.getId());
        assertNull(categoryDto.getName());
    }
}