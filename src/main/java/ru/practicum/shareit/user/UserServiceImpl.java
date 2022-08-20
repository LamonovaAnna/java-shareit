package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (checkEmailIsNotExist(userDto.getEmail()) && validateEmail(userDto.getEmail())) {
            return UserMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
        }
        log.info("Incorrect email");
        throw new EmailAlreadyExistException(userDto.getEmail());
    }

    @Override
    public UserDto updateUser(UserDto userDto, long userId) {
        if (userDto.getEmail() != null) {
            if (checkEmailIsNotExist(userDto.getEmail()) && validateEmail(userDto.getEmail())) {
                return UserMapper.toUserDto(userStorage.updateUser(UserMapper.toUpdateUser(
                        userStorage.findUserById(userId), UserMapper.toUser(userDto))));
            }
            log.info("Incorrect email");
            throw new EmailAlreadyExistException(userDto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.updateUser(UserMapper.toUpdateUser(
                userStorage.findUserById(userId), UserMapper.toUser(userDto))));
    }


    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toUsersDto(userStorage.getAllUsers());
    }

    @Override
    public UserDto findUserById(long id) {
        return UserMapper.toUserDto(userStorage.findUserById(id));
    }

    @Override
    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }

    private boolean checkEmailIsNotExist(String email) {
        for (UserDto userDto : getAllUsers()) {
            if (userDto.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Incorrect email");
        }
        return true;
    }
}
