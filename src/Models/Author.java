package Models;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table
public class Author extends Model {
    @PrimaryKey
    @Column(name="id", type = ColumnType.INTEGER)
    public int id;

    @Column(name="authorName", type = ColumnType.VARCHAR, length = 30, unique = true)
    public String authorName;
}
