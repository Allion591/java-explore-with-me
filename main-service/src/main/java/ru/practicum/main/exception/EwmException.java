package ru.practicum.main.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EwmException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public EwmException(String message, HttpStatus status, String reason) {
        super(message);
        this.status = status;
        this.reason = reason;
    }
}