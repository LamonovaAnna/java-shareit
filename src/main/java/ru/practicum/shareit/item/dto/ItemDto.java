package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;

    @JsonProperty(value = "available")
    private Boolean isAvailable;

    private Long requestId;
}
