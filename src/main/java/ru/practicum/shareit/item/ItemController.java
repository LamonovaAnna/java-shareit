package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping()
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable(value = "id") long itemId) {
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping
    public List<ItemBookingDto> getAllItemsByOwner(@RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/{id}")
    public ItemBookingDto findItemById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                           @PathVariable(value = "id") long itemId) {
        return itemService.findItemById(itemId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable(value = "id") long itemId) {
        itemService.deleteItem(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByNameOrDescription(@RequestParam(value = "text", required = false) String text) {
        return itemService.findItemsByNameOrDescription(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createCommentToItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestBody CommentDto commentDto,
                                          @PathVariable long itemId) {
        return itemService.createCommentToItem(userId, commentDto, itemId);
    }
}
