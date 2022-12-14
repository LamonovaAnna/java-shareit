package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final UserDto userDto = new UserDto(null, "test", "test@yandex.ru");
    private final UserDto savedUser1 = new UserDto(1L, "test1", "test@yandex.ru");
    private final UserDto savedUser2 = new UserDto(2L, "test2", "test@mail.ru");

    @Test
    void test1_createUser() throws Exception {
        Mockito
                .when(userService.createUser(userDto))
                .thenReturn(savedUser1);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(savedUser1.getName())))
                .andExpect(jsonPath("$.email", is(savedUser1.getEmail())));

        Mockito.verify(userService, times(1)).createUser(userDto);
    }

    @Test
    void test2_createIncorrectUser() throws Exception {
        userDto.setEmail("testyandex.ru");

        Mockito
                .when(userService.createUser(userDto))
                .thenThrow(ValidationException.class);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test3_getUsers() throws Exception {
        Mockito
                .when(userService.getAllUsers())
                .thenReturn(List.of(savedUser1, savedUser2));

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(savedUser1.getName())))
                .andExpect(jsonPath("$[0].email", is(savedUser1.getEmail())))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is(savedUser2.getName())))
                .andExpect(jsonPath("$[1].email", is(savedUser2.getEmail())));

        Mockito.verify(userService, times(1)).getAllUsers();
    }

    @Test
    void test4_getUsersWhenTheyAreNotPresent() throws Exception {
        Mockito
                .when(userService.getAllUsers())
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(userService, times(1)).getAllUsers();
    }

    @Test
    void test5_findUserById() throws Exception {
        Mockito
                .when(userService.findUserById(1L))
                .thenReturn(savedUser1);

        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(savedUser1.getName())))
                .andExpect(jsonPath("$.email", is(savedUser1.getEmail())));

        Mockito.verify(userService, times(1)).findUserById(1L);
    }

    @Test
    void test6_findUserByIncorrectId() throws Exception {
        Mockito
                .when(userService.findUserById(100L))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/users/100")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void test7_deleteUser() throws Exception {
        Mockito
                .doNothing()
                .when(userService).deleteUser(1L);

        mvc.perform(delete("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void test8_updateUser() throws Exception {
        UserDto userForUpdate = new UserDto(null, "update", null);
        savedUser1.setName(userForUpdate.getName());
        Mockito
                .when(userService.updateUser(userForUpdate, 1L))
                .thenReturn(savedUser1);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userForUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(savedUser1.getName())))
                .andExpect(jsonPath("$.email", is(savedUser1.getEmail())));

        Mockito.verify(userService, times(1)).updateUser(userForUpdate, 1L);
    }

    @Test
    void test9_createUserDuplicateEmail() throws Exception {
        Mockito
                .when(userService.createUser(Mockito.any(UserDto.class)))
                .thenThrow(RuntimeException.class);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
