import Models.Restaurant;
import Models.Testing;
import Models.Users;
import core.Model;
import filters.Filter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Users user = Model.objects(Users.class).get(Filter.eq("id", 2));
        user.name = "Mahammad";
        user.save();
    }
}
