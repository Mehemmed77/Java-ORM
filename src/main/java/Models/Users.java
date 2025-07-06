package Models;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table
public class Users extends Model {
    @Column(name = "id", type = ColumnType.INTEGER)
    public int id;

    @Column(name = "name", type = ColumnType.VARCHAR)
    public String name;

    @Column(name = "email", type = ColumnType.VARCHAR)
    public String email;

    @Column(name = "age", type = ColumnType.INTEGER, nullable = true)
    public Integer age;

    @Override
    public String toString() {
        return "User: " + name + " " + email + " " + age;
    }
}
