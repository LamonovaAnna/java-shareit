package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final BookingShortDto booking = new BookingShortDto(null, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(5), null, null, BookingStatus.WAITING);
    private final BookingShortDto savedBooking = new BookingShortDto(1L, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(5), 1L, 2L, BookingStatus.WAITING);
    private final BookingDto savedBookingDto = new BookingDto(1L, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(5), new ItemShortDto(1L, "Bicycle"),
            new UserShortDto(2L, "Tom"), BookingStatus.WAITING);

    @Test
    void test1_createBooking() throws Exception {
        Mockito
                .when(bookingService.createBooking(booking, 2L))
                .thenReturn(savedBooking);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start", Matchers.is(Matchers.notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end", Matchers.is(Matchers.notNullValue())))
                .andExpect(jsonPath("$.itemId", is(savedBooking.getItemId().intValue())))
                .andExpect(jsonPath("$.bookerId", is(savedBooking.getBookerId().intValue())))
                .andExpect(jsonPath("$.status", is(savedBooking.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1)).createBooking(booking, 2L);
    }

    @Test
    void test2_findBookingById() throws Exception {
        Mockito
                .when(bookingService.findBookingById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(savedBookingDto);

        mvc.perform(MockMvcRequestBuilders.get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start", Matchers.is(Matchers.notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end", Matchers.is(Matchers.notNullValue())))
                .andExpect(jsonPath("$.status", is(savedBookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(savedBookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(savedBookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(savedBookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(savedBookingDto.getBooker().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .findBookingById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void test3_findBookingsByBooker() throws Exception {
        Mockito
                .when(bookingService.findBookingsByBooker(2L, "FUTURE", 0, 10))
                .thenReturn(List.of(savedBookingDto));

        mvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "FUTURE")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start", Matchers.is(Matchers.notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("[0].end", Matchers.is(Matchers.notNullValue())))
                .andExpect(jsonPath("$[0].status", is(savedBookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.id", is(savedBookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$[0].item.name", is(savedBookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(savedBookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$[0].booker.name", is(savedBookingDto.getBooker().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .findBookingsByBooker(2L, "FUTURE", 0, 10);
    }

    @Test
    void test4_findBookingsByOwner() throws Exception {
        Mockito
                .when(bookingService.findBookingsByOwner(1L, "FUTURE", 0, 10))
                .thenReturn(List.of(savedBookingDto));

        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start", Matchers.is(Matchers.notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("[0].end", Matchers.is(Matchers.notNullValue())))
                .andExpect(jsonPath("$[0].status", is(savedBookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.id", is(savedBookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$[0].item.name", is(savedBookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(savedBookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$[0].booker.name", is(savedBookingDto.getBooker().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .findBookingsByOwner(1L, "FUTURE", 0, 10);
    }

    @Test
    void test5_approveOrRejectBooking() throws Exception {
        savedBookingDto.setStatus(BookingStatus.APPROVED);

        Mockito
                .when(bookingService.approveOrRejectBooking(1L, 1L, true))
                .thenReturn(savedBookingDto);

        mvc.perform(MockMvcRequestBuilders.patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start", Matchers.is(Matchers.notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end", Matchers.is(Matchers.notNullValue())))
                .andExpect(jsonPath("$.status", is(savedBookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(savedBookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(savedBookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(savedBookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(savedBookingDto.getBooker().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .approveOrRejectBooking(1L, 1L, true);

    }
}