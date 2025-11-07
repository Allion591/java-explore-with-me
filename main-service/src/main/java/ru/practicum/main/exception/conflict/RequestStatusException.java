package ru.practicum.main.exception.conflict;

public class RequestStatusException extends RuntimeException {
  public RequestStatusException(String message) {
    super(message);
  }
}
