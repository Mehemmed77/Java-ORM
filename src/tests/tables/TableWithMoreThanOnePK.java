package tests.tables;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class TableWithMoreThanOnePK extends Model {
    @Column(name="id1", type=ColumnType.INTEGER)
    private int id1;

    @Column(name="id2", type=ColumnType.INTEGER)
    private int id2;

    @Column(name="num", type=ColumnType.INTEGER)
    private int num;

    public TableWithMoreThanOnePK(){}

    public TableWithMoreThanOnePK(int num) {
        this.num = num;
    }
}
