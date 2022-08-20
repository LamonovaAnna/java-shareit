package ru.practicum.shareit.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private long id;

    @NotNull
    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private long requestorId;

    private LocalDateTime created = LocalDateTime.now();
}
