package ru.practicum.main.dto.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NewUserRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void newUserRequest_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        NewUserRequest userRequest = NewUserRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // When
        String json = objectMapper.writeValueAsString(userRequest);
        NewUserRequest deserialized = objectMapper.readValue(json, NewUserRequest.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John Doe\""));
        assertTrue(json.contains("\"email\":\"john.doe@example.com\""));
        assertEquals(userRequest.getName(), deserialized.getName());
        assertEquals(userRequest.getEmail(), deserialized.getEmail());
    }

    @Test
    void newUserRequest_ShouldHandleDifferentData() throws JsonProcessingException {
        // Given
        String json = "{\"name\":\"Jane Smith\",\"email\":\"jane.smith@test.org\"}";

        // When
        NewUserRequest userRequest = objectMapper.readValue(json, NewUserRequest.class);

        // Then
        assertEquals("Jane Smith", userRequest.getName());
        assertEquals("jane.smith@test.org", userRequest.getEmail());
    }

    @Test
    void newUserRequest_ShouldHaveLombokFunctionality() {
        // Given & When
        NewUserRequest user1 = NewUserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        NewUserRequest user2 = NewUserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        NewUserRequest user3 = NewUserRequest.builder()
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
        user1.setName("Updated Name");
        user1.setEmail("updated@example.com");

        assertEquals("Updated Name", user1.getName());
        assertEquals("updated@example.com", user1.getEmail());
    }
}