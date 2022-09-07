package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static Item toItem(ItemDto itemDto, long ownerId) {
        if (itemDto == null) {
            return null;
        }
        return Item.builder()
                .id(itemDto.getId())
                .ownerId(ownerId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .requestId(itemDto.getRequestId() != null ? itemDto.getRequestId() : null)
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDto.builder()
                .id(item.getId())
                .ownerId(item.getOwnerId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.getIsAvailable())
                .requestId(item.getRequestId() != null ? item.getRequestId() : null)
                .build();
    }

    public static Item toUpdateItem(Item item, Item updateItem) {
        item.setName(updateItem.getName() == null ? item.getName() : updateItem.getName());
        item.setDescription(updateItem.getDescription() == null ? item.getDescription() : updateItem.getDescription());
        item.setIsAvailable(updateItem.getIsAvailable() == null ? item.getIsAvailable() : updateItem.getIsAvailable());
        return item;
    }

    public static List<ItemDto> toItemsDto(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}