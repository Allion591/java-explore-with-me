package ru.practicum.main.exception.validation;

public class UserEmailConflictException extends RuntimeException {
  public UserEmailConflictException(String message) {
    super(message);
  }
}
