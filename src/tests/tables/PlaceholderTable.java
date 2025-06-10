package tests.tables;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table(name="PlaceholderTable")
public class PlaceholderTable extends Model {
    @Column(name="id", type= ColumnType.INTEGER, primaryKey = true, autoIncrement = true)
    private int id;

    public PlaceholderTable() {}
}
