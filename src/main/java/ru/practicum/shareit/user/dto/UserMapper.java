package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId() != null ? userDto.getId() : null);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    public static UserDto toUserDto(User user) {
        return user == null ? null : new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static UserShortDto toUserShortDto(User user) {
        return user == null ? null : new UserShortDto(user.getId(), user.getName());
    }

    public static User toUpdateUser(User user, User updateUser) {
        user.setName(updateUser.getName() == null ? user.getName() : updateUser.getName());
        user.setEmail(updateUser.getEmail() == null ? user.getEmail() : updateUser.getEmail());
        return user;
    }

    public static List<UserDto> toUsersDto(List<User> users) {
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
