package manager;
import core.Model;

public class QuerySet<T> {
    private Class<T> modelClass;

    public QuerySet(Class<T> clazz) {
        modelClass = clazz;
    }
}
