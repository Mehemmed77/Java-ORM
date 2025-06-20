package tests.tables;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class TableWithNoPrimaryKey extends Model {
    @Column(name="age", type=ColumnType.INTEGER)
    private int age;

    public TableWithNoPrimaryKey(){}

    public TableWithNoPrimaryKey(int age){
        this.age = age;
    }
}
