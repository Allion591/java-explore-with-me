package ru.practicum.main.exception.conflict;

public class DuplicateRequestException extends RuntimeException {
  public DuplicateRequestException(String message) {
    super(message);
  }
}
