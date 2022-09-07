package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long userId, long itemId);

    List<ItemDto> getAllItems();

    List<ItemDto> getAllItemsByOwner(long userId);

    ItemDto findItemById(long itemId);

    void deleteItem(long itemId, long userId);

    List<ItemDto> findItemsByNameOrDescription(String text);
}
