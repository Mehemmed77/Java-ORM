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
        Restaurant r1 = new Restaurant().set("restaurantName", "kkkjdkjfjdkks");
        Restaurant r2 = new Restaurant().set("restaurantName", "kkkkk");

        r1.save();
        r1.save();
    }
}
