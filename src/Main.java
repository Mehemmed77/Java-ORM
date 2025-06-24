import Models.Author;
import Models.Users;
import core.Model;
import filters.Filter;

public class Main {
    public static void main(String[] args) {
        Author author = Model.objects(Author.class).get(Filter.eq("id", 1));
        author.authorName = "Hugo Viktor";
        author.update();
    }
}
