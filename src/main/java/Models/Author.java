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

    public Author(String authorName) {
        this.authorName = authorName;
    }

    public int getId() {
        return this.id;
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public Author() {}
@Override
public String toString() {
    return "Author{ id=" + id + ", authorName=" + authorName + "}";
}
}