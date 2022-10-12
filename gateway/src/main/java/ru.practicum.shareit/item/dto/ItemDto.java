package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private Long ownerId;

    @Size(max = 20)
    private String name;

    @Size(max = 500)
    private String description;

    @JsonProperty(value = "available")
    private Boolean isAvailable;

    private Long requestId;
}
