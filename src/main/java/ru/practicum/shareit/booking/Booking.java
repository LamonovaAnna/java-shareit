package ru.practicum.shareit.booking;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class Booking {
    private long id;

    @FutureOrPresent
    private LocalDate startBooking;

    @Future
    private LocalDate endBooking;

    @NotNull
    private long itemId;

    @NotNull
    private long bookerId;

    @NotNull
    private BookingStatus status;
}
