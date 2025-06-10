package tests.tables;
import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table(name = "TestTable")
public class TestTable extends Model {
    @Column(name="id", type = ColumnType.INTEGER, primaryKey = true, autoIncrement = true)
    private int id;

    @Column(name="name", type = ColumnType.VARCHAR)
    private String name;

    @Column(name="age", type = ColumnType.INTEGER)
    private int age;

    public TestTable(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
