package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private Long ownerId;

    @NonNull
    @NotBlank
    @Size(max = 20)
    private String name;

    @NonNull
    @NotBlank
    @Size(max = 500)
    private String description;

    @NonNull
    @JsonProperty(value = "available")
    private Boolean isAvailable;

    private Long requestId;
}
