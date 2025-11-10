package ru.practicum.main.exception.conflict;

public class EventNotEditableException extends ConflictException {
    public EventNotEditableException(String message) {
        super(message);
    }
}