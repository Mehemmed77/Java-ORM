package Models;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class Restaurant extends Model {
    @Column(name = "id", type = ColumnType.INT, primaryKey = true)
    public int id;

    @Column(name = "RestaurantName", type = ColumnType.VARCHAR, nullable = false, unique = true, length = 30)
    public String restaurantName;
}
