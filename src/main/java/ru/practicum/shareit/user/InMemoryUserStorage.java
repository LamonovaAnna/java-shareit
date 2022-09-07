package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    private Long incrementId() {
        return id++;
    }

    @Override
    public User createUser(User user) {
        user.setId(incrementId());
        users.put(user.getId(), user);
        log.info("User was created with id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.info("User with id {} wasn't found", user.getId());
            throw new UserNotFoundException();
        }
        users.put(user.getId(), user);
        log.info("User with id {} was updated", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(long id) {
        if (!users.containsKey(id)) {
            log.info("User with id {} wasn't found", id);
            throw new UserNotFoundException();
        }
        return users.get(id);
    }

    @Override
    public void deleteUser(long id) {
        if (!users.containsKey(id)) {
            log.info("User with id {} wasn't found", id);
            throw new UserNotFoundException();
        }
        log.info("User with id {} was removed", id);
        users.remove(id);
    }
}
