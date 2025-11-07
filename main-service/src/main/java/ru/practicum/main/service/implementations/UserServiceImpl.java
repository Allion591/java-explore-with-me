package ru.practicum.main.service.implimentations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.exception.notFound.UserNotFoundException;
import ru.practicum.main.exception.validation.UserEmailConflictException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.interfaces.UserService;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание нового пользователя: {}", newUserRequest.getEmail());

        // Проверка уникальности email
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            log.error("Email уже используется");
            throw new UserEmailConflictException(newUserRequest.getEmail());
        }

        User user = userMapper.toUser(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("Создан пользователь с id: {}", savedUser.getId());
        return userMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей с ids: {}, from: {}, size: {}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findByIdIn(ids, pageable).getContent();
        }

        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id: {} не найден", userId);
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с id {} удален", userId);
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("Получение пользователя по id: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id: {} не найден в базе", userId);
            throw new UserNotFoundException(userId);
        }
    }
}