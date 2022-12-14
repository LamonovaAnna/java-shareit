package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final ItemDto item = new ItemDto(1L, 1L, "Bicycle", "Really fast bicycle",
            true, null);
    private final UserShortDto requester = new UserShortDto(2L, "Tom");
    private final ItemRequestDto request = new ItemRequestDto(null, "I need some bicycle", null,
            LocalDateTime.now());
    private final ItemRequestDto savedRequest = new ItemRequestDto(1L, "I need some bicycle", requester,
            LocalDateTime.now());
    private final ItemRequestWithItemsDto requestWithItem = new ItemRequestWithItemsDto(1L, "I need some bicycle",
            requester, LocalDateTime.now(), List.of(item));

    @Test
    void test1_createItemRequest() throws Exception {
        Mockito
                .when(itemRequestService.createItemRequest(request, 2L))
                .thenReturn(savedRequest);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is(savedRequest.getDescription())))
                .andExpect(jsonPath("$.requester.id", is(savedRequest.getRequester().getId().intValue())))
                .andExpect(jsonPath("$.requester.name", is(savedRequest.getRequester().getName())))
                .andExpect(jsonPath("$.created", is(notNullValue())));

        Mockito.verify(itemRequestService, times(1)).createItemRequest(request, 2L);
    }

    @Test
    void test2_getAllRequestsByRequester() throws Exception {
        Mockito
                .when(itemRequestService.getAllRequestsByRequester(2L))
                .thenReturn(List.of(requestWithItem));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is(savedRequest.getDescription())))
                .andExpect(jsonPath("$[0].requester.id", is(savedRequest.getRequester().getId().intValue())))
                .andExpect(jsonPath("$[0].requester.name", is(savedRequest.getRequester().getName())))
                .andExpect(jsonPath("$[0].created", is(notNullValue())))
                .andExpect(jsonPath("$[0].items[0]", is(notNullValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(item.getName())));

        Mockito.verify(itemRequestService, times(1)).getAllRequestsByRequester(2L);
    }

    @Test
    void test3_getAllRequestsWithPagination() throws Exception {
        Mockito
                .when(itemRequestService.getAllRequestsWithPagination(1L, 0, 10))
                .thenReturn(List.of(requestWithItem));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is(savedRequest.getDescription())))
                .andExpect(jsonPath("$[0].requester.id", is(savedRequest.getRequester().getId().intValue())))
                .andExpect(jsonPath("$[0].requester.name", is(savedRequest.getRequester().getName())))
                .andExpect(jsonPath("$[0].created", is(notNullValue())))
                .andExpect(jsonPath("$[0].items[0]", is(notNullValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(item.getName())));

        Mockito.verify(itemRequestService, times(1))
                .getAllRequestsWithPagination(1L, 0, 10);
    }

    @Test
    void test4_getRequestById() throws Exception {
        Mockito
                .when(itemRequestService.getRequestById(1L, 1L))
                .thenReturn(requestWithItem);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is(savedRequest.getDescription())))
                .andExpect(jsonPath("$.requester.id", is(savedRequest.getRequester().getId().intValue())))
                .andExpect(jsonPath("$.requester.name", is(savedRequest.getRequester().getName())))
                .andExpect(jsonPath("$.created", is(notNullValue())))
                .andExpect(jsonPath("$.items[0]", is(notNullValue())))
                .andExpect(jsonPath("$.items[0].name", is(item.getName())));

        Mockito.verify(itemRequestService, times(1)).getRequestById(1L, 1L);
    }
}
