package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, long userId);

    List<UserDto> getAllUsers();

    UserDto findUserById(long id);

    void deleteUser(long id);
}
