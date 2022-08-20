package ru.practicum.shareit.item;

import java.util.List;

public interface ItemStorage {

    Item createItem(Item item);

    Item updateItem(Item item);

    List<Item> getAllItems();

    Item findItemById(long id);

    void deleteItem(long id);
}
