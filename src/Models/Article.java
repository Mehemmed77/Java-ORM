package Models;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class Article extends Model {
    @PrimaryKey
    @Column(name = "id", type = ColumnType.INTEGER)
    public int id;


}
