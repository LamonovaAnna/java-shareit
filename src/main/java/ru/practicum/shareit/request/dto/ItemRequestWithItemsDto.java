package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@Getter
@Setter
public class ItemRequestWithItemsDto {
    private Long id;
    private String description;
    private UserShortDto requester;
    private LocalDateTime created;
    private List<ItemDto> items;
}
