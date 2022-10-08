package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {

    BookingShortDto createBooking(BookingShortDto bookingDto, long bookerId);

    BookingDto findBookingById(long bookingId, long userId);

    List<BookingDto> findBookingsByBooker(long bookerId, String state, Integer from, Integer size);

    List<BookingDto> findBookingsByOwner(long ownerId, String state, Integer from, Integer size);

    BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean isApproved);

}
