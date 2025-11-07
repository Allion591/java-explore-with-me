package ru.practicum.main.exception;

public class CompilationNotFoundException extends NotFoundException {
    public CompilationNotFoundException(Long compilationId) {
        super(String.format("Подборка с id=%d не найдена", compilationId));
    }
}