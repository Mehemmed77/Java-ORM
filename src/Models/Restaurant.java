package Models;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class Restaurant extends Model {
    @Column(name = "id", type = ColumnType.INTEGER, autoIncrement = true, primaryKey = true)
    public int id;

    @Column(name = "RestaurantName", type = ColumnType.VARCHAR, nullable = false, unique = true, length = 30)
    public String restaurantName;

    public Restaurant(){}

    public Restaurant(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    @Override
    public String toString() {
        return "Restaurant: " + this.id + " " + this.restaurantName;
    }
}
