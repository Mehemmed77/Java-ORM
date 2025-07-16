package Models;

import annotations.Column;
import annotations.ForeignKey;
import annotations.PrimaryKey;
import annotations.Table;
import core.Model;
import enums.ColumnType;

@Table
public class Comments extends Model {
    @PrimaryKey
    @Column(name = "id", type = ColumnType.INTEGER)
    public int id;

    @Column(name = "comment", type = ColumnType.VARCHAR, length = 200)
    public String comment;

    @ForeignKey(reference = Article.class, relatedName = "comments")
    public Article article;

    @Override
    public String toString() {
        return "Comments{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", article=" + article+
                '}';
    }

    public int getId() {
        return this.id;
    }

    public String getComment() {
        return this.comment;
    }

    public Article getArticle() {
        return this.article;
    }

}