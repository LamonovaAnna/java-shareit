package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectUserIdException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) {
        if (isUserExists(userId) && isItemValid(itemDto)) {
            return ItemMapper.toItemDto(itemStorage.createItem(ItemMapper.toItem(itemDto, userId)));
        }
        log.info("User not found");
        throw new UserNotFoundException();
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        if (isUserExists(userId) && itemStorage.findItemById(itemId).getOwnerId() == userId) {
            return ItemMapper.toItemDto(itemStorage.updateItem(ItemMapper.toUpdateItem(
                    itemStorage.findItemById(itemId), ItemMapper.toItem(itemDto, userId))));
        }
        log.info("Incorrect user id");
        throw new IncorrectUserIdException();
    }

    @Override
    public List<ItemDto> getAllItems() {
        return ItemMapper.toItemsDto(itemStorage.getAllItems());
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(long userId) {
        if (isUserExists(userId)) {
            return ItemMapper.toItemsDto(itemStorage.getAllItems())
                    .stream()
                    .filter(itemDto -> itemDto.getOwnerId() == userId)
                    .collect(Collectors.toList());
        }
        log.info("User not found");
        throw new UserNotFoundException();
    }

    @Override
    public ItemDto findItemById(long itemId) {
        return ItemMapper.toItemDto(itemStorage.findItemById(itemId));
    }

    @Override
    public void deleteItem(long itemId, long userId) {
        if (isUserExists(userId) && itemStorage.findItemById(itemId).getOwnerId() == userId) {
            itemStorage.deleteItem(itemId);
        }
        log.info("Incorrect user id");
        throw new IncorrectUserIdException();
    }

    @Override
    public List<ItemDto> findItemsByNameOrDescription(String text) {
        if (text != null && !text.isBlank()) {
            String textForSearch = text.toLowerCase();
            return ItemMapper.toItemsDto(itemStorage.getAllItems())
                    .stream()
                    .filter(i -> i.getName().toLowerCase().contains(textForSearch)
                            || i.getDescription().toLowerCase().contains(textForSearch))
                    .filter(ItemDto::getIsAvailable)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    private boolean isUserExists(long userId) {
        return userStorage.findUserById(userId) != null;
    }

    private boolean isItemValid(ItemDto item) {
        if (item.getName() == null || item.getName().isBlank()) {
            log.info("Field \"name\" doesn't filled");
            throw new ValidationException("Incorrect item name");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.info("Field \"description\" doesn't filled");
            throw new ValidationException("Incorrect item description");
        }
        if (item.getIsAvailable() == null) {
            log.info("Field \"available\" doesn't filled");
            throw new ValidationException("Field \"isAvailable\" must be filled");
        }
        return true;
    }
}
