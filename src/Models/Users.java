package Models;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table
public class Users extends Model {
    @Column(name="id", type= ColumnType.INTEGER, primaryKey = true, autoIncrement = true)
    public int id;

    @Column(name = "username", type = ColumnType.VARCHAR, length = 50)
    public String username;

    @Column(name = "age", type = ColumnType.INTEGER)
    public int age;

    public Users(){}

    public Users(String username, int age) {
        this.username = username;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User: " + username + " " + age;
    }
}
