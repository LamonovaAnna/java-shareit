package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class CommentMapper {

    public static Comment toComment(CommentDto commentDto, Long authorId, Long itemId) {
        if (commentDto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setItem(ItemMapper.toItem(new ItemDto(itemId, null, null, null,
                null, null), null));
        comment.setAuthor(UserMapper.toUser(new UserDto(authorId, null, null)));
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(comment.getId(),
                comment.getText(),
                new ItemShortDto(comment.getId(), null),
                null,
                comment.getCreated());
    }

    public static CommentShortDto toCommentShortDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentShortDto(comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }

    public static Set<CommentShortDto> toCommentsDto(Set<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toSet());
    }
}
