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
        Restaurant r = new Restaurant().set("restaurantName", "Mado");
        r.save();
        System.out.println(r.id);
    }
}
