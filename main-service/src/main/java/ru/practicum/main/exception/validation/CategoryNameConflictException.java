package ru.practicum.main.exception.validation;

public class CategoryNameConflictException extends RuntimeException {
  public CategoryNameConflictException(String message) {
    super(message);
  }
}
