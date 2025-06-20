package tests.tables;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class DummyTable extends Model {
    @Column(name="id", type= ColumnType.INTEGER, primaryKey = true, autoIncrement = true)
    private int id;

    @Column(name="username", type=ColumnType.VARCHAR, nullable = false, unique = true)
    private String username;

    @Column(name="age", type=ColumnType.INTEGER, nullable = false)
    private int age;

    public DummyTable() {}

    public DummyTable(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
