package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserShortDto {
    private Long id;

    @NonNull
    @NotBlank
    private String name;
}
