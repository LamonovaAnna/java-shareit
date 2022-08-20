package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    private Long incrementId() {
        return id++;
    }

    @Override
    public Item createItem(Item item) {
        item.setId(incrementId());
        items.put(item.getId(), item);
        log.info("Item was created with id {}", item.getId());
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (!items.containsKey(item.getId())) {
            log.info("Item with id {} wasn't found", item.getId());
            throw new ItemNotFoundException();
        }
        items.put(item.getId(), item);
        log.info("Item with id {} was updated", item.getId());
        return item;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item findItemById(long id) {
        if (!items.containsKey(id)) {
            log.info("Item with id {} wasn't found", id);
            throw new ItemNotFoundException();
        }
        return items.get(id);
    }

    @Override
    public void deleteItem(long id) {
        if (!items.containsKey(id)) {
            log.info("Item with id {} wasn't found", id);
            throw new ItemNotFoundException();
        }
        log.info("Item with id {} was removed", id);
        items.remove(id);
    }
}
