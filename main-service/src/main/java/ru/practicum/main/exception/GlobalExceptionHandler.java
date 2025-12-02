package ru.practicum.main.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.main.dto.error.ApiError;
import ru.practicum.main.exception.conflict.ConflictException;
import ru.practicum.main.exception.notFound.NotFoundException;
import ru.practicum.main.exception.validation.ValidationException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BAD_REQUEST_REASON = "Неверный запрос.";
    private static final String INTEGRITY_VIOLATION_REASON = "Нарушение целостности данных.";
    private static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера.";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.warn("NotFoundException: {}", e.getMessage());
        return buildApiError(e.getStatus(), e.getReason(), e.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.warn("ConflictException: {}", e.getMessage());
        return buildApiError(e.getStatus(), e.getReason(), e.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());
        String message = extractIntegrityViolationMessage(e);

        return buildApiError(HttpStatus.CONFLICT, INTEGRITY_VIOLATION_REASON, message, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("Неизвестный статус")) {
            return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON,
                    e.getMessage(), null);
        }
        return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON,
                "Некорректные параметры запроса", null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(Exception e) {
        log.warn("Validation exception: {}", e.getMessage());

        List<String> errors = extractValidationErrors(e);
        return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, "Ошибка валидации данных.", null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleParameterExceptions(Exception e) {
        log.warn("Parameter exception: {}", e.getMessage());

        String message = extractParameterErrorMessage(e);
        return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, message, null);
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCustomValidationException(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return buildApiError(e.getStatus(), e.getReason(), e.getMessage(), null);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.warn("HandlerMethodValidationException: {}", e.getMessage());

        String errorMessage = "Ошибка валидации";
        if (!e.getAllValidationResults().isEmpty()) {
            errorMessage = e.getAllValidationResults().getFirst().toString();
        }

        return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, errorMessage, null);
    }

    @ExceptionHandler(EwmException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleEwmException(EwmException e) {
        log.error("EwmException: {}", e.getMessage(), e);
        return buildApiError(e.getStatus(), e.getReason(), e.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());

        String message = extractJsonErrorMessage(e);
        return buildApiError(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, message, null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOtherExceptions(Exception e) {
        log.error("Unexpected exception: ", e);
        return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера",
                INTERNAL_ERROR_MESSAGE, null);
    }

    private ApiError buildApiError(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
                .build();
    }

    private List<String> extractValidationErrors(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            return ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                            error.getField(), error.getDefaultMessage(),
                            maskSensitiveValue(error.getField(), error.getRejectedValue())))
                    .collect(Collectors.toList());
        } else if (e instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) e;
            return ex.getConstraintViolations().stream()
                    .map(violation -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                            violation.getPropertyPath(), violation.getMessage(),
                            maskSensitiveValue(violation.getPropertyPath().toString(), violation.getInvalidValue())))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private String extractParameterErrorMessage(Exception e) {
        if (e instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException ex = (MethodArgumentTypeMismatchException) e;
            return String.format("Неверный тип параметра '%s'. Ожидается: %s",
                    ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестно");
        } else if (e instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException ex = (MissingServletRequestParameterException) e;
            return String.format("Отсутствует обязательный параметр: %s", ex.getParameterName());
        }
        return "Ошибка в параметрах запроса";
    }

    private String extractJsonErrorMessage(HttpMessageNotReadableException e) {
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Тело запроса отсутствует")) {
                return "Тело запроса обязательно";
            } else if (e.getMessage().contains("JSON parse error")) {
                return "Неверный формат JSON в теле запроса";
            }
        }
        return "Ошибка чтения тела запроса";
    }

    private String extractIntegrityViolationMessage(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("unique constraint") || message.contains("Duplicate entry")) {
                return "Нарушение уникальности данных";
            } else if (message.contains("foreign key constraint")) {
                return "Нарушение ссылочной целостности";
            } else if (message.contains("not null")) {
                return "Обязательное поле не может быть пустым";
            }
        }
        return "Нарушение целостности данных";
    }

    private Object maskSensitiveValue(String fieldName, Object value) {
        // Маскировка чувствительных данных
        if (fieldName != null && value != null) {
            String lowerField = fieldName.toLowerCase();
            if (lowerField.contains("password") || lowerField.contains("token") || lowerField.contains("secret")) {
                return "***";
            }
        }
        return value;
    }
}