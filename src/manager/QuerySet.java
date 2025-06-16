package manager;
import customErrors.GetReturnedLessThanOneRowException;
import customErrors.GetReturnedMoreThanOneRowException;
import database.DatabaseManager;
import filters.Filter;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerySet<T> {
    private String tableName;
    private Class<T> modelClass;
    private List<ColumnInfo> columnInfos;
    private Constructor<T> constructor;

    public QuerySet(Class<T> clazz, String tableName, List<ColumnInfo> columnInfos) {
        try{
            modelClass = clazz;
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.tableName = tableName;
            this.columnInfos = columnInfos;

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private T hydrateSingleInstance(Map<String, Object> data) {
        try{
            T instance = constructor.newInstance();

            for(ColumnInfo col : columnInfos) {
                String columnName = col.column().name();
                Field field = col.field();

                Object value = data.get(columnName);
                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public List<T> getAll() {
        List<T> rows = new ArrayList<>();
        String selectAllScript = GenerateSQLScripts.getAllRowsScript(this.tableName);
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectAndFetch(selectAllScript);

        for(Map<String, Object> data: results) {
            rows.add(hydrateSingleInstance(data));
        }

        return rows;
    }

    public T get(Filter filter) {
        String script = GenerateSQLScripts.generateParameterizedSelect(tableName, filter.toSQL());
        List<Map<String, Object>> results = DatabaseManager.getInstance().
                executePreparedSelectAndFetch(filter.getParameters(), script);

        if (results.isEmpty()) throw new GetReturnedLessThanOneRowException("Get returned 0 columns, must return exactly one. Use filter instead.");
        if (results.size() > 1) throw new GetReturnedMoreThanOneRowException("Get returned more than one column must return exactly one. Use filter instead.");

        return hydrateSingleInstance(results.get(0));
    }
}
