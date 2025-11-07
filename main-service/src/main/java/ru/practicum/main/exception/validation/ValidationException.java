package ru.practicum.main.exception.validation;

import org.springframework.http.HttpStatus;
import ru.practicum.main.exception.EwmException;

public class ValidationException extends EwmException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Неправильно составленный запрос.");
    }
}