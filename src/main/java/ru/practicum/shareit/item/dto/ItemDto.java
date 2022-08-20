package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
