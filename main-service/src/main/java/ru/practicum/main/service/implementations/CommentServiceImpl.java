package ru.practicum.main.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.exception.conflict.ConflictException;
import ru.practicum.main.exception.notFound.NotFoundException;
import ru.practicum.main.exception.validation.ValidationException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CommentsRepository;
import ru.practicum.main.service.interfaces.CommentService;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentServiceImpl implements CommentService {

    private final UserService userService;
    private final EventService eventService;
    private final CommentsRepository repository;
    private final CommentMapper mapper;

    // Админский метод для модерации
    @Override
    @Transactional
    public CommentResponseDto updateCommentByAdmin(Long commentId, UpdateCommentAdminRequest request) {
        log.info("Admin сервис: Принял запрос на обновление статуса комментария: id {}, status {}", commentId,
                request.getStateAction());

        Comment comment = findByIdCheck(commentId);

        validateCommentEditable(comment);
        // Сохраняем старое состояние для логирования
        CommentStatus oldState = comment.getState();

        // Обновляем статус в зависимости какой передан
        switch (request.getStateAction()) {
            case "APPROVE" -> comment.setState(CommentStatus.APPROVED);
            case "REJECT" -> comment.setState(CommentStatus.REJECTED);
            default -> throw new IllegalArgumentException("Неизвестный статус: " + request.getStateAction());
        }

        // Сохраняем только при изменении
        if (oldState != comment.getState()) {
            Comment updatedComment = repository.save(comment);
            log.info("Статус комментария {} изменен: {} -> {}",
                    commentId, oldState, updatedComment.getState());
            return mapper.toCommentResponseDto(updatedComment);
        } else {
            log.info("Статус комментария {} не изменился: {}", commentId, oldState);
            return mapper.toCommentResponseDto(comment);
        }
    }

    // Админскии метод для получения всех комментариев ожидающих модерацию
    @Override
    public List<CommentResponseDto> getCommentsForModeration(int page, int size) {
        log.info("Admin сервис: Запрос комментариев для модерации (PENDING и EDITED)");

        Pageable pageable = PageRequest.of(page, size, Sort.by("created").ascending());

        List<CommentStatus> moderationStatuses = Arrays.asList(
                CommentStatus.PENDING,
                CommentStatus.EDITED
        );

        List<Comment> commentsForModeration = repository.findByStateIn(moderationStatuses, pageable).getContent();

        log.debug("Найдено {} комментариев для модерации", commentsForModeration.size());

        return commentsForModeration.stream()
                .map(mapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    // Метод сохранения комментариев для авторизованных пользователей
    @Override
    @Transactional
    public CommentResponseDto saveComment(Long userId, Long eventId, CommentDto commentDto) {
        log.info("Private сервис: Принял запрос на сохранение комментария, пользователь id: {}, событие id: {}",
                userId, eventId);

        Comment newComment = mapper.toComment(commentDto);
        // Проверяем существует ли событие и пользователь
        Event event = eventService.getEventById(eventId);

        // Проверяем опубликовано ли событие
        if (event.getState() != (EventState.PUBLISHED)) {
            throw new ConflictException("Событие еще не опубликовано");
        }

        newComment.setEvent(event);
        newComment.setAuthor(userService.getUserById(userId));

        log.info("Private сервис: Комментарии, пользователь id: {}, событие id: {}",
                newComment.getAuthor().getId(), newComment.getEvent().getId());

        Comment savedComment = repository.save(newComment);

        return mapper.toCommentResponseDto(savedComment);
    }

    // Получение комментария создателем
    @Override
    public CommentResponseDto getCommentById(Long userId, Long commentId) {
        log.info("Private сервис: Принял запрос на вывод комментария, пользователь id: {}, комментарий id: {}",
                userId, commentId);

        userService.checkUserExists(userId);

        Comment comment = findByIdCheck(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException("Комментарий не принадлежит пользователю");
        }

        return mapper.toCommentResponseDto(comment);
    }

    // Получение всех комментариев пользователя
    @Override
    public List<CommentResponseDto> getUserComments(Long userId, int from, int size) {
        log.info("Private сервис: Принял запрос на вывод всех комментариев пользователя с id: {}", userId);
        userService.checkUserExists(userId);

        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());

        List<Comment> userComments = repository.findByAuthorId(userId, pageable).getContent();

        if (userComments.isEmpty()) {
            log.debug("Пользователь {} не имеет комментариев", userId);
            return Collections.emptyList();
        }

        return userComments.stream()
                .map(mapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto updateCommentByUser(Long userId, Long commentId, CommentDto commentDto) {
        log.info("Private сервис: Принял запрос на обновление комментария id: {}, пользователя с id: {}",
                commentId, userId);
        userService.checkUserExists(userId);

        Comment comment = findByIdCheck(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("Пользователь {} пытается изменить чужой комментарий {}", userId, commentId);
            throw new ConflictException("Нельзя менять чужой комментарий");
        }

        validateCommentEditable(comment);

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        if (comment.getState() == CommentStatus.APPROVED || comment.getState() == CommentStatus.PENDING) {
            comment.setState(CommentStatus.EDITED);
        }

        Comment updatedComment = repository.save(comment);

        log.info("Комментарий {} успешно обновлен пользователем {}", commentId, userId);
        return mapper.toCommentResponseDto(updatedComment);
    }

    // Удаление комментария пользователем
    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Private сервис: Принял запрос на удаление комментария id: {}, пользователя с id: {}",
                commentId, userId);

        userService.checkUserExists(userId);

        Comment comment = findByIdCheck(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("Пользователь {} пытается удалить чужой комментарий {}", userId, commentId);
            throw new ConflictException("Нельзя удалять чужой комментарий");
        }

        if (comment.getState() == CommentStatus.DELETED) {
            log.warn("Пользователь {} пытается удалить уже удаленный комментарий {}", userId, commentId);
            throw new ConflictException("Комментарий уже удален");
        }

        comment.setState(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());

        repository.save(comment);
        log.info("Private сервис: Пользователь {} удалил свой комментарий {}", userId, commentId);
    }

    // Получение всех комментариев события для неавторизованных пользователей
    @Override
    public List<CommentResponseDto> getAllByEventId(Long eventId, int from, int size) {
        log.info("Public сервис: Принял запрос на вывод всех APPROVED комментариев события id: {}", eventId);

        eventService.existsById(eventId);

        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());

        List<Comment> eventComments = repository.findByEventIdAndState(eventId,
                CommentStatus.APPROVED, pageable).getContent();

        if (eventComments.isEmpty()) {
            log.debug("Событие {} не имеет APPROVED комментариев", eventId);
            return Collections.emptyList();
        }

        log.info("Найдено {} APPROVED коментариев для события {}", eventComments.size(), eventId);
        return eventComments.stream()
                .map(mapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    private Comment findByIdCheck(Long commentId) {
        return repository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий не найден: {}", commentId);
                    return new NotFoundException("Комментарий не найден");
                });
    }

    private void validateCommentEditable(Comment comment) {
        // Проверка, что комментарий не удален
        if (comment.getState() == CommentStatus.DELETED) {
            log.warn("Попытка редактирования удаленного коментария id: {}", comment.getId());
            throw new ValidationException("Нельзя редактировать удаленный комментарий");
        }
    }
}