package ru.practicum.main.exception.validation;

import ru.practicum.main.exception.conflict.ConflictException;

public class CategoryNameConflictException extends ConflictException {
    public CategoryNameConflictException(String name) {
        super(String.format("Название категории %s уже существует", name));
    }
}