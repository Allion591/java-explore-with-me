package ru.practicum.main.exception.conflict;

public class RequestStatusException extends ConflictException {
    public RequestStatusException(String message) {
        super(message);
    }
}