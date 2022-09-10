package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
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
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getItem().getId(),
                comment.getAuthorId(),
                comment.getCreated());
    }

    public static Set<CommentDto> toCommentsDto(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toSet());
    }
}
