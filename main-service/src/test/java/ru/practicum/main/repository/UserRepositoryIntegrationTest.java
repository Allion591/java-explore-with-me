package ru.practicum.main.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.main.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        userRepository.deleteAll();

        // Создаем тестовых пользователей
        user1 = User.builder()
                .name("User One")
                .email("user1@email.com")
                .build();

        user2 = User.builder()
                .name("User Two")
                .email("user2@email.com")
                .build();

        user3 = User.builder()
                .name("User Three")
                .email("user3@email.com")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));
    }

    @Test
    void findByIdIn_withIds_shouldReturnFilteredUsers() {
        Page<User> result = userRepository.findByIdIn(
                List.of(user1.getId(), user3.getId()),
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .anyMatch(user -> user.getId().equals(user1.getId())));
        assertTrue(result.getContent().stream()
                .anyMatch(user -> user.getId().equals(user3.getId())));
    }

    @Test
    void findByIdIn_withNullIds_shouldReturnAllUsers() {
        Page<User> result = userRepository.findByIdIn(
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(3, result.getContent().size());
    }

    @Test
    void findByIdIn_withPagination_shouldReturnPagedResults() {
        Page<User> result = userRepository.findByIdIn(
                null,
                PageRequest.of(0, 2)
        );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("user1@email.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_whenEmailNotExists_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@email.com");

        assertFalse(exists);
    }

    @Test
    void findByEmail_whenEmailExists_shouldReturnUser() {
        Optional<User> result = userRepository.findByEmail("user2@email.com");

        assertTrue(result.isPresent());
        assertEquals("User Two", result.get().getName());
        assertEquals("user2@email.com", result.get().getEmail());
    }

    @Test
    void findByEmail_whenEmailNotExists_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_withPageable_shouldReturnPagedUsers() {
        Page<User> result = userRepository.findAll(PageRequest.of(0, 2));

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findAll_withSecondPage_shouldReturnRemainingUsers() {
        Page<User> firstPage = userRepository.findAll(PageRequest.of(0, 2));
        Page<User> secondPage = userRepository.findAll(PageRequest.of(1, 2));

        assertEquals(2, firstPage.getContent().size());
        assertEquals(1, secondPage.getContent().size());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(3, secondPage.getTotalElements());
    }

    @Test
    void saveUser_shouldPersistUserCorrectly() {
        User newUser = User.builder()
                .name("New User")
                .email("newuser@email.com")
                .build();

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("New User", savedUser.getName());
        assertEquals("newuser@email.com", savedUser.getEmail());
    }

    @Test
    void updateUser_shouldUpdateUserCorrectly() {
        User userToUpdate = userRepository.findById(user1.getId()).orElseThrow();
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("updated@email.com");

        User updatedUser = userRepository.save(userToUpdate);

        assertEquals(user1.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@email.com", updatedUser.getEmail());
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabase() {
        userRepository.deleteById(user2.getId());

        Optional<User> deletedUser = userRepository.findById(user2.getId());
        assertFalse(deletedUser.isPresent());

        Page<User> remainingUsers = userRepository.findAll(PageRequest.of(0, 10));
        assertEquals(2, remainingUsers.getContent().size());
    }
}