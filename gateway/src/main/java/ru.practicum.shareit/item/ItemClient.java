package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
@Slf4j
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto itemDto) {
        checkItemValid(itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long userId, ItemDto itemDto, long itemId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getAllItemsByOwner(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> findItemById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> deleteItem(long userId, long itemId) {
        return delete("/" + itemId, userId);
    }

    public ResponseEntity<Object> findItemsByNameOrDescription(String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, parameters);
    }

    public ResponseEntity<Object> createCommentToItem(long userId, CommentDto commentDto, long itemId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }

    private void checkItemValid(ItemDto item) {
        if (item.getName() == null || item.getName().isBlank()) {
            log.info("Field \"item name\" doesn't filled");
            throw new ValidationException("Incorrect item name");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.info("Field \"item description\" doesn't filled");
            throw new ValidationException("Incorrect item description");
        }
        if (item.getIsAvailable() == null) {
            log.info("Field \"available\" doesn't filled");
            throw new ValidationException("Field \"isAvailable\" must be filled");
        }
    }
}
