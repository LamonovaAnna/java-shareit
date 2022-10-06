package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static Booking toBooking(BookingShortDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId() != null ? bookingDto.getId() : null);
        booking.setStartBooking(bookingDto.getStart());
        booking.setEndBooking(bookingDto.getEnd());
        booking.setItem(ItemMapper.toItem(new ItemDto(bookingDto.getItemId(), null, null, null,
                null, null), null));
        booking.setBooker(UserMapper.toUser(new UserDto(bookingDto.getBookerId(), null, null)));
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return booking == null ? null : new BookingShortDto(booking.getId(),
                booking.getStartBooking(),
                booking.getEndBooking(),
                booking.getItem().getId(),
                booking.getBooker().getId(),
                booking.getStatus());
    }

    public static BookingDto toBookingDto(Booking booking) {
        return booking == null ? null : new BookingDto(booking.getId(),
                booking.getStartBooking(),
                booking.getEndBooking(),
                new ItemShortDto(booking.getItem().getId(), booking.getItem().getName()),
                new UserShortDto(booking.getBooker().getId(), booking.getBooker().getName()),
                booking.getStatus());
    }

    public static BookingForItemDto toBookingForItemDto(Booking booking) {
        return booking == null ? null : new BookingForItemDto(booking.getId(),
                booking.getBooker().getId());
    }

    public static List<BookingDto> toBookingsDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
