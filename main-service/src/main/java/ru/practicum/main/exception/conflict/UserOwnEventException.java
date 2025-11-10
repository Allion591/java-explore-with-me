package ru.practicum.main.exception.conflict;

public class UserOwnEventException extends ConflictException {
    public UserOwnEventException() {
        super("Вы не можете подать заявку на участие в вашем собственном мероприятии");
    }
}