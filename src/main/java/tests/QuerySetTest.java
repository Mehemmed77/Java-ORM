package tests;

import Models.Users;
import core.Model;
import filters.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuerySetTest {
    @BeforeEach
    public void setup() {
        Model.dropTable(Users.class);
        Model.createTable(Users.class);
    }

    @Test
    public void testInsertAndGetAll() {
        Users user = new Users();
        user.name = "Alice";
        user.email = "alice@example.com";
        user.age = 25;

        user.save();

        List<Users> users = Model.objects(Users.class).getAll();
        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).name);
    }

    @Test
    public void testUpdateToNull() {
        Users user = new Users();
        user.name = "Bob";
        user.email = "bob@example.com";
        user.age = 30;
        user.save();

        Map<String, Object> mp = new HashMap<>();
        mp.put("age", null);

        int updated = Model.objects(Users.class).update(mp,
                Filter.eq("name", "Bob")
        );
        assertEquals(1, updated);

        Users updatedUser = Model.objects(Users.class).get(Filter.eq("name", "Bob"));
        assertNull(updatedUser.age);
    }

    @Test
    public void testExists() {
        Users user1 = new Users().set("name", "a").set("email", "alice@example.com");
        user1.save();

        assertTrue(Model.objects(Users.class).exists(Filter.eq("email", "alice@example.com")));
        assertFalse(Model.objects(Users.class).exists(Filter.eq("email", "ghost@example.com")));
    }

    @Test
    public void testCounts() {
        Users user1 = new Users().set("name", "a").set("email", "a");
        Users user2 = new Users().set("name", "Bob").set("email", "a");
        user1.save();user2.save();

        int total = Model.objects(Users.class).countAll();
        int onlyBob = Model.objects(Users.class).count(Filter.eq("name", "Bob"));

        assertEquals(1, onlyBob);
        assertEquals(2, total);
    }
}
