package ru.practicum.main.admins;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.controller.admins.AdminUserController;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.service.interfaces.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    void createUser_ShouldReturnCreatedUser() {
        NewUserRequest request = new NewUserRequest("John Doe", "john@example.com");
        UserDto expectedResponse = new UserDto(1L, "John Doe", "john@example.com");

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(expectedResponse);

        UserDto actualResponse = adminUserController.createUser(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        verify(userService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void getUsers_WithIds_ShouldReturnFilteredUsers() {
        List<Long> ids = List.of(1L, 2L);
        Integer from = 0;
        Integer size = 10;

        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        when(userService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of(user1, user2));

        List<UserDto> result = adminUserController.getUsers(ids, from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService, times(1)).getUsers(ids, from, size);
    }

    @Test
    void getUsers_WithoutIds_ShouldReturnAllUsers() {
        Integer from = 0;
        Integer size = 10;

        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        when(userService.getUsers(isNull(), anyInt(), anyInt())).thenReturn(List.of(user1, user2));

        List<UserDto> result = adminUserController.getUsers(null, from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService, times(1)).getUsers(null, from, size);
    }

    @Test
    void deleteUser_ShouldCallService() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        assertDoesNotThrow(() -> adminUserController.deleteUser(userId));
        verify(userService, times(1)).deleteUser(userId);
    }
}