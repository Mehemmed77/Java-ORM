package Models;

import annotations.Column;
import annotations.ForeignKey;
import annotations.PrimaryKey;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table()
public class Article extends Model {
    @PrimaryKey
    @Column(name = "id", type = ColumnType.INTEGER)
    public int id;

    @Column(name = "content", type = ColumnType.TEXT)
    public String content;

    @ForeignKey(reference = Author.class)
    public Author author;

    public Author getAuthor() {
        return getRelated("author");
    }


    public int getId() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }
@Override
public String toString() {
    return "Article{ id=" + id + ", content=" + content + ", author=" + getRelated(\"author\") + "}";
}
}