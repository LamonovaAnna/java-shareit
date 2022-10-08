package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long userId, long itemId);

    List<ItemBookingDto> getAllItemsByOwner(long userId, Integer from, Integer size);

    ItemBookingDto findItemById(long itemId, Long userId);

    void deleteItem(long itemId, long userId);

    List<ItemDto> findItemsByNameOrDescription(String text, Integer from, Integer size);

    CommentDto createCommentToItem(long authorId, CommentDto commentDto, long itemId);
}
