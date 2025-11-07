package ru.practicum.main.exception.notFound;

import org.springframework.http.HttpStatus;
import ru.practicum.main.exception.EwmException;

public class NotFoundException extends EwmException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Искомый объект не был найден.");
    }
}