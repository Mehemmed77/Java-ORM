import Models.Article;
import Models.Author;
import Models.Users;
import core.Model;
import filters.Filter;

public class Main {
    public static void main(String[] args) {
        Model.dropTable(Author.class);
        Model.createTable(Author.class);
    }
}
