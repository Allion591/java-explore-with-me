package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.model.Comment;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByStateIn(List<CommentStatus> states, Pageable pageable);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Page<Comment> findByEventIdAndState(Long eventId, CommentStatus state, Pageable pageable);
}