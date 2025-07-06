package tests.tables;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class TableWithInvalidColumnName extends Model {
    @Column(name="id", type=ColumnType.INTEGER)
    private int id;

    @Column(name="Invalid name", type=ColumnType.VARCHAR)
    private String invalidName;

    public TableWithInvalidColumnName(){}

    public TableWithInvalidColumnName(String invalidName) {
        this.invalidName = invalidName;
    }
}
