package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingShortDto createBooking(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                         @RequestBody BookingShortDto bookingDto) {
        return bookingService.createBooking(bookingDto, bookerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long bookingId) {
        return bookingService.findBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findBookingsByBooker(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                 @RequestParam(defaultValue = "ALL", required = false) String state,
                                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return bookingService.findBookingsByBooker(bookerId, state, from, size)
                .stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> findBookingsByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                @RequestParam(defaultValue = "ALL", required = false) String state,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        return bookingService.findBookingsByOwner(ownerId, state ,from, size)
                .stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
    }

    @PatchMapping("/{bookingId}")
    public BookingDto confirmOrRejectBooking(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                             @PathVariable long bookingId,
                                             @RequestParam boolean approved) {
        return bookingService.approveOrRejectBooking(ownerId, bookingId, approved);
    }
}
