package boku.business.user;

import com.google.inject.Singleton;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Singleton
public class Users {
    private final ConcurrentHashMap<User.UserId, User> users = new ConcurrentHashMap<>();

    public Optional<User> get(User.UserId id) {
        return Optional.ofNullable(users.get(id));
    }

    public void save(User user) {
        users.put(user.id, user);
    }
}
