package tests.tables;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table(name="PlaceholderTable")
public class PlaceholderTable extends Model {
    @Column(name="id", type= ColumnType.INTEGER)
    private int id;

    @Column(name="name", type = ColumnType.VARCHAR, length = 40)
    private String name;

    public PlaceholderTable() {}

    public PlaceholderTable(String name) {
        this.name = name;
    }
}
