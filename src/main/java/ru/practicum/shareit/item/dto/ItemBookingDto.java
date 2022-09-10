package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemBookingDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;

    @JsonProperty(value = "available")
    private Boolean isAvailable;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
}
