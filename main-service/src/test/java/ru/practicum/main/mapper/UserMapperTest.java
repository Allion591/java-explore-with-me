package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toUserDto_ShouldMapUserToUserDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // When
        UserDto userDto = userMapper.toUserDto(user);

        // Then
        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void toUserShortDto_ShouldMapUserToUserShortDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // When
        UserShortDto userShortDto = userMapper.toUserShortDto(user);

        // Then
        assertNotNull(userShortDto);
        assertEquals(user.getId(), userShortDto.getId());
        assertEquals(user.getName(), userShortDto.getName());
        // Email не должен быть в UserShortDto
    }

    @Test
    void toUser_ShouldMapNewUserRequestToUser() {
        // Given
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .build();

        // When
        User user = userMapper.toUser(newUserRequest);

        // Then
        assertNotNull(user);
        assertNull(user.getId()); // id должен быть проигнорирован
        assertNull(user.getEvents()); // events должен быть проигнорирован
        assertNull(user.getRequests()); // requests должен быть проигнорирован
        assertNull(user.getCreatedOn()); // createdOn должен быть проигнорирован
        assertEquals(newUserRequest.getName(), user.getName());
        assertEquals(newUserRequest.getEmail(), user.getEmail());
    }

    @Test
    void toUserDto_ShouldHandleNull() {
        // When
        UserDto userDto = userMapper.toUserDto(null);

        // Then
        assertNull(userDto);
    }

    @Test
    void toUserShortDto_ShouldHandleNull() {
        // When
        UserShortDto userShortDto = userMapper.toUserShortDto(null);

        // Then
        assertNull(userShortDto);
    }

    @Test
    void toUser_ShouldHandleNull() {
        // When
        User user = userMapper.toUser(null);

        // Then
        assertNull(user);
    }

    @Test
    void toUserShortDto_ShouldNotIncludeEmail() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        // When
        UserShortDto userShortDto = userMapper.toUserShortDto(user);

        // Then - UserShortDto не должен содержать email
        assertEquals(1L, userShortDto.getId());
        assertEquals("John Doe", userShortDto.getName());
        // Проверяем, что в toString нет email (косвенная проверка)
        assertFalse(userShortDto.toString().contains("john.doe@example.com"));
    }

    @Test
    void toUser_ShouldIgnoreSpecifiedFields() {
        // Given
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        // When
        User user = userMapper.toUser(newUserRequest);

        // Then - проверяем, что указанные поля игнорируются
        assertNull(user.getId());
        assertNull(user.getEvents());
        assertNull(user.getRequests());
        assertNull(user.getCreatedOn());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }
}