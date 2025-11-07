package ru.practicum.main.exception;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long categoryId) {
        super(String.format("Категория с id=%d не найдена", categoryId));
    }
}