package ru.practicum.main.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super(String.format("Пользователь с id=%d не найден", userId));
    }
}