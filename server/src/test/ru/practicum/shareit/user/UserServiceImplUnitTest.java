package ru.practicum.shareit.user;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private final User firstUser = makeUser(1L, "test", "user@yandex.ru");
    private final User secondUser = makeUser(2L, "test", "user2@yandex.ru");

    @Test
    void test1_createCorrectUser() {
        Mockito
                .when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(firstUser);

        UserDto user = userService.createUser(UserMapper.toUserDto(firstUser));

        assertThat(user).isNotNull();
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    void test2_getUserByCorrectId() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(firstUser));

        UserDto user = userService.findUserById(1L);

        assertThat(user).isNotNull();
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    void test3_getUserByIncorrectId() {
        Mockito
                .when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(100L),
                "Incorrect exception");

        Mockito.verify(userRepository, Mockito.times(1)).findById(100L);
    }

    @Test
    void test4_getAllUsers() {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        List<UserDto> users = userService.getAllUsers();

        AssertionsForClassTypes.assertThat(users).isNotNull();
        AssertionsForClassTypes.assertThat(users.size()).isEqualTo(2);
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    void test5_deleteUser() {
        Mockito
                .doNothing()
                .when(userRepository)
                .deleteById(1L);

        userService.deleteUser(1L);

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    private static User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}