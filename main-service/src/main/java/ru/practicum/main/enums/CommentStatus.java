package ru.practicum.main.enums;

public enum CommentStatus {
    PENDING,      // Ожидает модерации
    APPROVED,     // Одобрен, виден всем
    REJECTED,     // Отклонен модератором
    DELETED,      // Удален
    EDITED,       // Отредактирован, требует повторной модерации
}