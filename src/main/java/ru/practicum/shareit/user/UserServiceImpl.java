package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
        if (validateEmail(userDto.getEmail())) {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        }
        log.info("Incorrect email");
        throw new EmailAlreadyExistException(userDto.getEmail());
    }

    @Override
    public UserDto updateUser(UserDto userDto, long userId) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUpdateUser(
                userRepository.getReferenceById(userId), UserMapper.toUser(userDto))));

    }

    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.toUsersDto(userRepository.findAll());
    }

    @Override
    public UserDto findUserById(long id) {
        if (userRepository.existsById(id)) {
            return UserMapper.toUserDto(userRepository.getReferenceById(id));
        }
        throw new UserNotFoundException();
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    private boolean validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Incorrect email");
        }
        return true;
    }
}
