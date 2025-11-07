package ru.practicum.main.exception.database;

public class DataRetrievalException extends RuntimeException {
  public DataRetrievalException(String message) {
    super(message);
  }
}
