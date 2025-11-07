package ru.practicum.main.exception.conflict;

public class DuplicateRequestException extends ConflictException {
    public DuplicateRequestException() {
        super("Вы уже подали заявку на участие в этом мероприятии");
    }
}