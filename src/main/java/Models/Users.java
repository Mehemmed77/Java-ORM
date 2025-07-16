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

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public Integer getAge() {
        return this.age;
    }
@Override
public String toString() {
    return "Users{ id=" + id + ", name=" + name + ", email=" + email + ", age=" + age + "}";
}
}