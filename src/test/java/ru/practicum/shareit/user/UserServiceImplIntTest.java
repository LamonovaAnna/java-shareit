package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplIntTest {

    private final UserService userService;

    @MethodSource("test1MethodSource")
    @ParameterizedTest
    void test1_updateCorrectUser(UserDto user) {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);

        UserDto updatedUser = userService.updateUser(user, savedUser.getId());

        assertNotNull(updatedUser, "User wasn't updated");
        assertEquals(savedUser.getId(), updatedUser.getId(), "Incorrect Id");
        if (user.getName() != null) {
            assertEquals(user.getName(), updatedUser.getName(), "Incorrect name");
            assertEquals(savedUser.getEmail(), updatedUser.getEmail(), "Incorrect email");
        }
        if (user.getEmail() != null) {
            assertEquals(saveUser.getName(), updatedUser.getName(), "Incorrect name");
            assertEquals(user.getEmail(), updatedUser.getEmail(), "Incorrect email");
        }
    }

    private static Stream<Arguments> test1MethodSource() {
        return Stream.of(
                Arguments.of(makeUser("update", null)),
                Arguments.of(makeUser(null, "update@mail.ru"))
        );
    }

    @Test
    void test2_updateIncorrectUserId() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);
        UserDto updateUser = makeUser("update", null);

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(updateUser, 100L));
        assertEquals("This user doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(updateUser, 100L),
                "Incorrect exception");
        assertEquals(userService.findUserById(savedUser.getId()).getName(), savedUser.getName(),
                "Incorrect name");
    }

    @Test
    void test3_findUserById() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);

        UserDto foundUser = userService.findUserById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId(), "Incorrect Id");
        assertEquals(saveUser.getName(), foundUser.getName(), "Incorrect name");
        assertEquals(saveUser.getEmail(), foundUser.getEmail(), "Incorrect email");
    }

    @Test
    void test4_findUserByIncorrectId() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        userService.createUser(saveUser);

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(100L));
        assertEquals("This user doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(100L),
                "Incorrect exception");
    }

    private static UserDto makeUser(String name, String email) {
        UserDto user = new UserDto();
        user.setId(null);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
