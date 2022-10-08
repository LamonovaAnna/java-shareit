package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplIntTest {

    private final UserService userService;

    @Test
    void test1_updateUserName() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);
        UserDto userForUpdate = makeUser("update", null);

        UserDto updatedUser = userService.updateUser(userForUpdate, savedUser.getId());

        assertNotNull(updatedUser, "User wasn't updated");
        assertEquals(savedUser.getId(), updatedUser.getId(), "Incorrect Id");
        assertEquals(userForUpdate.getName(), updatedUser.getName(), "Incorrect name");
        assertEquals(savedUser.getEmail(), updatedUser.getEmail(), "Incorrect email");

    }

    @Test
    void test2_updateUserEmail() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);
        UserDto userForUpdate = makeUser(null, "update@mail.ru");

        UserDto updatedUser = userService.updateUser(userForUpdate, savedUser.getId());

        assertNotNull(updatedUser, "User wasn't updated");
        assertEquals(savedUser.getId(), updatedUser.getId(), "Incorrect Id");
        assertEquals(saveUser.getName(), updatedUser.getName(), "Incorrect name");
        assertEquals(userForUpdate.getEmail(), updatedUser.getEmail(), "Incorrect email");
    }

    @Test
    void test3_updateIncorrectUserId() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        UserDto savedUser = userService.createUser(saveUser);
        UserDto updateUser = makeUser("update", null);

        final UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
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
    void test5_findUserByIncorrectId() {
        UserDto saveUser = makeUser("test", "test@yandex.ru");
        userService.createUser(saveUser);

        final UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
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
