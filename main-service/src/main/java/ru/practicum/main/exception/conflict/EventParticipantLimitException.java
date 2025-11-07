package ru.practicum.main.exception.conflict;

public class EventParticipantLimitException extends ConflictException {
    public EventParticipantLimitException() {
        super("Количество участников ограничено");
    }
}