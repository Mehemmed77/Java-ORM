package manager;
import customErrors.GetReturnedLessThanOneRowException;
import customErrors.GetReturnedMoreThanOneRowException;
import customErrors.NullFilterException;
import database.DatabaseManager;
import filters.Filter;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import utils.TimeStampManager;
import validators.ValueValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

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
        try {
            T instance = constructor.newInstance();

            for(ColumnInfo col : columnInfos) {
                String columnName = col.column().name();
                Field field = col.field();

                Object value = data.get(columnName);
                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return null;
    }

    public List<T> getAll() {
        List<T> rows = new ArrayList<>();
        String selectAllScript = GenerateSQLScripts.getAllRowsScript(this.tableName);
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectQuery(selectAllScript, null);

        for(Map<String, Object> data: results) {
            rows.add(hydrateSingleInstance(data));
        }

        return rows;
    }

    public boolean exists(Filter filter) {
        String script = GenerateSQLScripts.generateExistsScript(tableName, filter.toSQL());
        return DatabaseManager.getInstance().hasResults(script, filter.getParameters());
    }

    // returns number of all existing rows in the table.
    public int countAll() {
        String script = GenerateSQLScripts.generateCountAllScript(tableName);
        return DatabaseManager.getInstance().executeAndReturnCount(script, null);
    }

    public int count(Filter filter) {
        if (filter == null) throw new
                NullFilterException("Filter cannot be null, if you want to count all rows, use countAll() method.");

        String script = GenerateSQLScripts.generateCountScript(tableName, filter.toSQL());
        return DatabaseManager.getInstance().executeAndReturnCount(script, filter.getParameters());
    }

    public T get(Filter filter) {
        String script = GenerateSQLScripts.generateSelectScript(tableName, filter.toSQL());
        List<Map<String, Object>> results = DatabaseManager.getInstance().
                executeSelectQuery(script, filter.getParameters());

        if (results.isEmpty()) throw new GetReturnedLessThanOneRowException("Get returned 0 columns, must return exactly one. Use filter instead.");
        if (results.size() > 1) throw new GetReturnedMoreThanOneRowException("Get returned more than one column must return exactly one. Use filter instead.");

        return hydrateSingleInstance(results.get(0));
    }

    public List<T> filter(Filter f) {
        List<T> rows = new ArrayList<>();
        String script = GenerateSQLScripts.generateSelectScript(tableName, f.toSQL());
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectQuery(script, f.getParameters());

        for(Map<String, Object> data: results) {
            rows.add(hydrateSingleInstance(data));
        }

        return rows;
    }

    public int update(Map<String, Object> data, Filter filter) {
        if (filter == null) throw new
                NullFilterException("Filter cannot be null. If you want to update all rows, use updateAll() method.");

        ValueValidator.validate(data, columnInfos);
        TimeStampManager.validateNotManuallyUpdate(data.keySet());

        LinkedHashMap<String, Object> orderedData = new LinkedHashMap<>(data);
        orderedData.put("updated_at", TimeStampManager.now());

        String script = GenerateSQLScripts.generateUpdateScript(
                tableName,
                orderedData.keySet(),
                orderedData,
                filter.toSQL()
        );

        List<Object> allValues = new ArrayList<>(orderedData.values()
                .stream().filter(Objects::nonNull).toList());

        allValues.addAll(filter.getParameters());

        return DatabaseManager.getInstance().executeUpdate(script,
                allValues);
    }

    public int deleteAll() {
        System.out.println("WARNING: ALL ROWS WILL BE DELETED FROM THE TABLE: " + tableName);
        String script = GenerateSQLScripts.deleteALlScript(tableName);
        return DatabaseManager.getInstance().executeUpdate(script, null);
    }

    public int delete(Filter filter) {
        if (filter == null) throw new NullFilterException("Filter cannot be null. If you want to delete all rows, use deleteAll() method.");

        String script = GenerateSQLScripts.generateDeleteScript(tableName, filter.toSQL());

        return DatabaseManager.getInstance().executeUpdate(script, filter.getParameters());
    }
}
