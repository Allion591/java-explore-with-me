package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.comment.CommentResponseDto;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "authorId", source = "author.id")
    CommentResponseDto toCommentResponseDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    Comment toComment(CommentDto commentDto);
}