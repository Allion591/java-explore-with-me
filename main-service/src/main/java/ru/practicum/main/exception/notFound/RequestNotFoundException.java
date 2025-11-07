package ru.practicum.main.exception.notFound;

public class RequestNotFoundException extends NotFoundException {
    public RequestNotFoundException(Long requestId) {
        super(String.format("Запрс с id=%d не найден", requestId));
    }
}