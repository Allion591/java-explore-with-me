package ru.practicum.main.exception;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(Long eventId) {
        super(String.format("Событие с id=%d не найдено", eventId));
    }
}