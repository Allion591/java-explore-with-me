package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.exception.notFound.UserNotFoundException;
import ru.practicum.main.exception.validation.UserEmailConflictException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.implementations.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long userId = 1L;
    private final User user = User.builder()
            .id(userId)
            .name("Test User")
            .email("test@email.com")
            .build();
    private final NewUserRequest newUserRequest = NewUserRequest.builder()
            .name("Test User")
            .email("test@email.com")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(userId)
            .name("Test User")
            .email("test@email.com")
            .build();

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(newUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        // Act
        UserDto result = userService.createUser(newUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@email.com", result.getEmail());

        verify(userRepository).existsByEmail(newUserRequest.getEmail());
        verify(userMapper).toUser(newUserRequest);
        verify(userRepository).save(user);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserEmailConflictException.class, () ->
                userService.createUser(newUserRequest)
        );

        verify(userRepository).existsByEmail(newUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsers_withIds_shouldReturnFilteredUsers() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        List<User> users = List.of(
                User.builder().id(1L).name("User1").build(),
                User.builder().id(2L).name("User2").build()
        );
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findByIdIn(ids, PageRequest.of(0, 10))).thenReturn(userPage);
        when(userMapper.toUserDto(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserDto.builder().id(user.getId()).name(user.getName()).build();
        });

        // Act
        List<UserDto> result = userService.getUsers(ids, 0, 10);

        // Assert
        assertEquals(2, result.size());
        verify(userRepository).findByIdIn(ids, PageRequest.of(0, 10));
        verify(userMapper, times(2)).toUserDto(any(User.class));
    }

    @Test
    void getUsers_withoutIds_shouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(
                User.builder().id(1L).name("User1").build(),
                User.builder().id(2L).name("User2").build(),
                User.builder().id(3L).name("User3").build()
        );
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserDto(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserDto.builder().id(user.getId()).name(user.getName()).build();
        });

        // Act
        List<UserDto> result = userService.getUsers(null, 0, 10);

        // Assert
        assertEquals(3, result.size());
        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository, never()).findByIdIn(anyList(), any(Pageable.class));
    }

    @Test
    void getUsers_withEmptyIds_shouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(
                User.builder().id(1L).name("User1").build(),
                User.builder().id(2L).name("User2").build()
        );
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserDto(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserDto.builder().id(user.getId()).name(user.getName()).build();
        });

        // Act
        List<UserDto> result = userService.getUsers(List.of(), 0, 10);

        // Assert
        assertEquals(2, result.size());
        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository, never()).findByIdIn(anyList(), any(Pageable.class));
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                userService.deleteUser(userId)
        );

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void getUserById_shouldReturnUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                userService.getUserById(userId)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void checkUserExists_shouldNotThrowWhenUserExists() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> userService.checkUserExists(userId));
        verify(userRepository).existsById(userId);
    }

    @Test
    void checkUserExists_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                userService.checkUserExists(userId)
        );

        verify(userRepository).existsById(userId);
    }

    @Test
    void createUser_shouldMapUserCorrectly() {
        // Arrange
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(newUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        // Act
        userService.createUser(newUserRequest);

        // Assert
        verify(userMapper).toUser(newUserRequest);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void getUsers_shouldMapAllUsersToDto() {
        // Arrange
        List<User> users = List.of(
                User.builder().id(1L).name("User1").build(),
                User.builder().id(2L).name("User2").build()
        );
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserDto(any(User.class))).thenReturn(UserDto.builder().build());

        // Act
        List<UserDto> result = userService.getUsers(null, 0, 10);

        // Assert
        assertEquals(2, result.size());
        verify(userMapper, times(2)).toUserDto(any(User.class));
    }
}