import java.util.List;

import Models.Article;
import Models.Author;
import core.Model;
import manager.Related;
import orm.ORM;

public class Main {
    public static void main(String[] args) {
        ORM.init();

        List<Author> authors = Model.objects(Author.class).prefetchRelated("article_set");
        Author author = authors.get(0);

        List<Article> articles = Related.of(author).get("article_set");
        System.out.println(articles);
    }
}
