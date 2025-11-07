package ru.practicum.main.exception.conflict;

public class EventNotPublishedException extends ConflictException {
    public EventNotPublishedException() {
        super("Вы не можете участвовать в неопубликованном мероприятии");
    }
}