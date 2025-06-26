import Models.Article;
import Models.Author;
import Models.Users;
import core.Model;
import filters.Filter;

public class Main {
    public static void main(String[] args) {
        Author author = Model.objects(Author.class).get(Filter.eq("authorName", "Franz Kafka"));

        Article article = new Article().setSequentially(
                "Every morning, the clerk arrives at his office at precisely 8:15. " +
                        "He performs his duties flawlessly, yet no one acknowledges his " +
                        "existence. One day, he decides to vary his routine—he arrives at 8:16. " +
                        "The building collapses." +
                        " He stands outside, watching the dust settle, wondering " +
                        "if his punctuality was the only thing holding it together.", author
        );

        article.save();
    }
}
