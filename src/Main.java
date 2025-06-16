import Models.Restaurant;
import Models.Users;
import core.Model;
import filters.Filter;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Filter filter = Filter.and(Filter.eq("username", "Heydar"), Filter.eq("age", 19));
        Users userHeydar = Model.objects(Users.class).get(filter);

        System.out.println(userHeydar);
    }
}
