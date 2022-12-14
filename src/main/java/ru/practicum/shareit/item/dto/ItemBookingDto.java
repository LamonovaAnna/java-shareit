package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.Set;

@Data
@AllArgsConstructor
public class ItemBookingDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;

    @JsonProperty(value = "available")
    private Boolean isAvailable;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private Set<CommentShortDto> comments;
}
