package ru.practicum.main.exception.conflict;

public class CategoryNotEmptyException extends ConflictException {
    public CategoryNotEmptyException(Long categoryId) {
        super(String.format("Категория с id=%d не является пустой", categoryId));
    }
}