import Models.Article;
import Models.Author;
import Models.Comments;
import Models.Users;
import core.Model;
import filters.Filter;
import manager.Related;
import orm.ORM;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ORM.init();
// 1) Author
        Author author = new Author();
        author.authorName = "Ernest Hemingway";
        author.save();

// 2) Article
        Article article = new Article();
        article.content = "A story of an old fisherman...";
        article.author = author;
        article.save();
//
//// 3) Comments
        Comments c1 = new Comments();
        c1.comment = "Great article!";
        c1.article = article;
        c1.save();
//
        Comments c2 = new Comments();
        c2.comment = "Very inspiring.";
        c2.article = article;
        c2.save();

// Test relations
        List<Article> articles = Related.of(author).get("article_set");
        System.out.println("Articles: " + articles);

        List<Comments> comments = Related.of(article).get("");
        System.out.println("Comments: " + comments);

        for (Comments c : comments) {
            Article a = c.getRelated("article");
            Author aAuthor = a.getRelated("author");
            System.out.println("Comment: " + c.comment);
            System.out.println("By Author: " + aAuthor.authorName);
        }
    }
}
