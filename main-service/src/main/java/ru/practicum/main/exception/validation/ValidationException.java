package ru.practicum.main.exception.validation;

public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
