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
        System.out.println(Model.objects(Comments.class).selectRelated("article"));
    }
}
