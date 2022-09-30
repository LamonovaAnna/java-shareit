package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentShortDto {
    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
