package ru.practicum.main.exception.conflict;

import org.springframework.http.HttpStatus;
import ru.practicum.main.exception.EwmException;

public class ConflictException extends EwmException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "Для запрошенной операции условия не выполнены.");
    }
}