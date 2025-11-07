package ru.practicum.main.exception.validation;

import ru.practicum.main.exception.conflict.ConflictException;

public class UserEmailConflictException extends ConflictException {
    public UserEmailConflictException(String email) {
        super(String.format("Email %s уже занят", email));
    }
}