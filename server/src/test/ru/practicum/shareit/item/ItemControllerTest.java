package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final ItemDto item = new ItemDto(null, null, "Bicycle",
            "Really fast bicycle", true, null);
    private final ItemDto savedItem = new ItemDto(1L, 1L, "Bicycle", "Really fast bicycle",
            true, null);
    private final BookingForItemDto lastBooking = new BookingForItemDto(1L, 2L);
    private final BookingForItemDto nextBooking = new BookingForItemDto(1L, 2L);
    private final CommentShortDto shortComment = new CommentShortDto(1L, "Really great", "Piter",
            LocalDateTime.now().minusHours(1));
    private final ItemBookingDto itemWithBooking = new ItemBookingDto(1L, 1L, "Bicycle",
            "Really fast bicycle", true, lastBooking, nextBooking,
            new HashSet<>(Collections.singleton(shortComment)));
    private final CommentDto comment = new CommentDto(null, "Really great",
            null, "Piter", null);
    private final CommentDto savedComment = new CommentDto(1L, "Really great",
            new ItemShortDto(1L, "Bicycle"), "Piter", LocalDateTime.now());

    @Test
    void test1_createItem() throws Exception {
        Mockito
                .when(itemService.createItem(item, 1L))
                .thenReturn(savedItem);

        mvc.perform(MockMvcRequestBuilders.post("/items")
                        .content(mapper.writeValueAsString(item))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.ownerId", is(savedItem.getOwnerId().intValue())))
                .andExpect(jsonPath("$.name", is(savedItem.getName())))
                .andExpect(jsonPath("$.description", is(savedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(savedItem.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is(savedItem.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1)).createItem(item, 1L);
    }

    @Test
    void test2_updateItem() throws Exception {
        ItemDto itemForUpdate = new ItemDto(null, null, "update", null, null,
                null);
        savedItem.setName(itemForUpdate.getName());
        Mockito
                .when(itemService.updateItem(itemForUpdate, 1L, 1L))
                .thenReturn(savedItem);

        mvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .content(mapper.writeValueAsString(itemForUpdate))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.ownerId", is(savedItem.getOwnerId().intValue())))
                .andExpect(jsonPath("$.name", is(savedItem.getName())))
                .andExpect(jsonPath("$.description", is(savedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(savedItem.getIsAvailable())))
                .andExpect(jsonPath("$.requestId", is(savedItem.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1)).updateItem(itemForUpdate, 1L, 1L);
    }

    @Test
    void test3_getAllItemsByOwner() throws Exception {
        Mockito
                .when(itemService.getAllItemsByOwner(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(itemWithBooking));

        mvc.perform(MockMvcRequestBuilders.get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$[0].ownerId", is(itemWithBooking.getOwnerId().intValue())))
                .andExpect(jsonPath("$[0].name", is(itemWithBooking.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithBooking.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemWithBooking.getIsAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(
                        itemWithBooking.getLastBooking().getId().intValue())))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(
                        itemWithBooking.getLastBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$[0].nextBooking.id", is(
                        itemWithBooking.getNextBooking().getId().intValue())))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(
                        itemWithBooking.getNextBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$[0].comments[0].id", is(shortComment.getId().intValue())))
                .andExpect(jsonPath("$[0].comments[0].text", is(shortComment.getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(shortComment.getAuthorName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments[0].created", Matchers.is(Matchers.notNullValue())));

        Mockito.verify(itemService, Mockito.times(1)).getAllItemsByOwner(1L, 0, 10);

    }

    @Test
    void test4_findItemById() throws Exception {
        Mockito
                .when(itemService.findItemById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(itemWithBooking);

        mvc.perform(MockMvcRequestBuilders.get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.ownerId", is(itemWithBooking.getOwnerId().intValue())))
                .andExpect(jsonPath("$.name", is(itemWithBooking.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBooking.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithBooking.getIsAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(
                        itemWithBooking.getLastBooking().getId().intValue())))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(
                        itemWithBooking.getLastBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(
                        itemWithBooking.getNextBooking().getId().intValue())))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(
                        itemWithBooking.getNextBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$.comments[0].id", is(shortComment.getId().intValue())))
                .andExpect(jsonPath("$.comments[0].text", is(shortComment.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(shortComment.getAuthorName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[0].created", Matchers.is(Matchers.notNullValue())));

        Mockito.verify(itemService, Mockito.times(1)).findItemById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void test5_deleteItem() throws Exception {
        Mockito
                .doNothing()
                .when(itemService).deleteItem(Mockito.anyLong(), Mockito.anyLong());

        mvc.perform(MockMvcRequestBuilders.delete("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemService, Mockito.times(1)).deleteItem(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void test6_findItemsByNameOrDescription() throws Exception {
        Mockito
                .when(itemService.findItemsByNameOrDescription("fast", 0, 10))
                .thenReturn(List.of(savedItem));

        mvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", "fast")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$[0].ownerId", is(savedItem.getOwnerId().intValue())))
                .andExpect(jsonPath("$[0].name", is(savedItem.getName())))
                .andExpect(jsonPath("$[0].description", is(savedItem.getDescription())))
                .andExpect(jsonPath("$[0].available", is(savedItem.getIsAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(savedItem.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1)).findItemsByNameOrDescription(
                "fast", 0, 10);
    }

    @Test
    void test7_createCommentToItem() throws Exception {
        Mockito
                .when(itemService.createCommentToItem(
                        2L, comment, 1L))
                .thenReturn(savedComment);

        mvc.perform(MockMvcRequestBuilders.post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.text", is(savedComment.getText())))
                .andExpect(jsonPath("$.item.id", is(savedComment.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(savedComment.getItem().getName())))
                .andExpect(jsonPath("$.authorName", is(savedComment.getAuthorName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created", Matchers.is(Matchers.notNullValue())));

        Mockito.verify(itemService, Mockito.times(1))
                .createCommentToItem(2L, comment, 1L);
    }
}