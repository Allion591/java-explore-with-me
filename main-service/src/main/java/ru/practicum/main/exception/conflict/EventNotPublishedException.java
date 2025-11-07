package ru.practicum.main.exception.conflict;

public class EventNotPublishedException extends RuntimeException {
  public EventNotPublishedException(String message) {
    super(message);
  }
}
