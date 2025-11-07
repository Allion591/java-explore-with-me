package ru.practicum.main.exception.conflict;

public class CategoryNotEmptyException extends RuntimeException {
  public CategoryNotEmptyException(String message) {
    super(message);
  }
}
