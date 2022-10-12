package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));

    }

    @Override
    public UserDto updateUser(UserDto userDto, long userId) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUpdateUser(
                userRepository.findById(userId).orElseThrow(UserNotFoundException::new), UserMapper.toUser(userDto))));

    }

    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toUsersDto(userRepository.findAll());
    }

    @Override
    public UserDto findUserById(long id) {
        return UserMapper.toUserDto(userRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}