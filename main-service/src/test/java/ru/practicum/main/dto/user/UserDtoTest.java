package ru.practicum.main.dto.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userDto_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // When
        String json = objectMapper.writeValueAsString(userDto);
        UserDto deserialized = objectMapper.readValue(json, UserDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"John Doe\""));
        assertTrue(json.contains("\"email\":\"john.doe@example.com\""));
        assertEquals(userDto.getId(), deserialized.getId());
        assertEquals(userDto.getName(), deserialized.getName());
        assertEquals(userDto.getEmail(), deserialized.getEmail());
    }

    @Test
    void userDto_ShouldHaveLombokFunctionality() {
        // Given & When
        UserDto user1 = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto user2 = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto user3 = UserDto.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());

        assertNotNull(user1.toString());
        assertTrue(user1.toString().contains("John Doe"));
        assertTrue(user1.toString().contains("john@example.com"));

        // Проверка геттеров и сеттеров
        user1.setId(3L);
        user1.setName("Updated Name");
        user1.setEmail("updated@example.com");

        assertEquals(3L, user1.getId());
        assertEquals("Updated Name", user1.getName());
        assertEquals("updated@example.com", user1.getEmail());
    }
}