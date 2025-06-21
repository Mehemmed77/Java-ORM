package Models;

import annotations.Column;
import annotations.Table;
import core.Model;
import enums.ColumnType;

import java.sql.Timestamp;

@Table
public class Testing extends Model {
    @Column(name = "id", type = ColumnType.INTEGER, primaryKey = true, autoIncrement = true)
    public int id;

    @Column(name = "name", type = ColumnType.VARCHAR, length = 50)
    public String name;

    @Column(name = "updated_at", type = ColumnType.TIMESTAMP)
    public Timestamp updated_at;

    @Column(name = "created_at", type = ColumnType.TIMESTAMP)
    public Timestamp created_at;
}
