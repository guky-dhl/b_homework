package boku.business.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsersTest {

    final User user = new User(new User.UserId());
    final Users users = new Users();

    @Test
    public void should_find_user_by_id() {
        users.save(user);

        var result = users.get(user.id);

        assertTrue(result.isPresent());
        assertEquals(result.get(), user);
    }

}