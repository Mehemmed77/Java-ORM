import Models.Restaurant;
import core.Model;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Restaurant> restaurants = Model.objects(Restaurant.class).getAll();
        System.out.println(restaurants);
    }
}
