import Models.Article;
import Models.Author;
import Models.Users;
import core.Model;
import filters.Filter;
import manager.Related;
import orm.ORM;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ORM.init();
        Author author = Model.objects(Author.class).get(Filter.eq("authorName", "Ernest Hemingway"));
        System.out.println(author.id);
        List<Article> articles = Related.of(author).get("articles");
    }
}
