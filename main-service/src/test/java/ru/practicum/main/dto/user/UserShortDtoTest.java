package ru.practicum.main.dto.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserShortDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userShortDto_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        UserShortDto userShortDto = UserShortDto.builder()
                .id(1L)
                .name("John Doe")
                .build();

        // When
        String json = objectMapper.writeValueAsString(userShortDto);
        UserShortDto deserialized = objectMapper.readValue(json, UserShortDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"John Doe\""));
        assertEquals(userShortDto.getId(), deserialized.getId());
        assertEquals(userShortDto.getName(), deserialized.getName());
    }

    @Test
    void userShortDto_ShouldHaveLombokFunctionality() {
        // Given & When
        UserShortDto user1 = UserShortDto.builder()
                .id(1L)
                .name("John Doe")
                .build();

        UserShortDto user2 = UserShortDto.builder()
                .id(1L)
                .name("John Doe")
                .build();

        UserShortDto user3 = UserShortDto.builder()
                .id(2L)
                .name("Jane Smith")
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());

        assertNotNull(user1.toString());
        assertTrue(user1.toString().contains("John Doe"));

        // Проверка геттеров и сеттеров
        user1.setId(3L);
        user1.setName("Updated Name");

        assertEquals(3L, user1.getId());
        assertEquals("Updated Name", user1.getName());
    }
}