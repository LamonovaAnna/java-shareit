package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        Item item = new Item();
        item.setId(itemDto.getId() != null ? itemDto.getId() : null);
        item.setOwnerId(ownerId);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setIsAvailable(itemDto.getIsAvailable());
        item.setRequest(itemDto.getRequestId() == null ? item.getRequest() : new ItemRequest(
                itemDto.getRequestId(), null, null, null, null));
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        return item == null ? null : new ItemDto(item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null);
    }

    public static ItemBookingDto toItemBookingDto(Item item) {
        return item == null ? null : new ItemBookingDto(item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                null,
                null,
                new HashSet<>());
    }

    public static ItemShortDto toItemShortDto(Item item) {
        return item == null ? null : new ItemShortDto(item.getId(),
                item.getName());
    }


    public static Item toUpdateItem(Item item, Item updateItem) {
        item.setName(updateItem.getName() == null ? item.getName() : updateItem.getName());
        item.setDescription(updateItem.getDescription() == null ? item.getDescription() : updateItem.getDescription());
        item.setIsAvailable(updateItem.getIsAvailable() == null ? item.getIsAvailable() : updateItem.getIsAvailable());
        return item;
    }

    public static List<ItemBookingDto> toItemsBookingDto(List<Item> items) {
        return items.stream().map(ItemMapper::toItemBookingDto).collect(Collectors.toList());
    }

    public static List<ItemDto> toItemsDto(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}