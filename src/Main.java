import Models.Restaurant;
import core.Model;

public class Main {
    public static void main(String[] args) {
        Model.createTable(Restaurant.class);
    }
}
