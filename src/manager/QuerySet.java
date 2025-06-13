package manager;
import annotations.Column;
import core.Model;
import database.DatabaseManager;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerySet<T> {
    private String tableName;
    private Class<T> modelClass;
    private List<ColumnInfo> columnInfos;

    public QuerySet(Class<T> clazz, String tableName, List<ColumnInfo> columnInfos) {
        modelClass = clazz;
        this.tableName = tableName;
        this.columnInfos = columnInfos;
    }

    public List<T> getAll() {
        List<T> rows = new ArrayList<>();
        String selectAllScript = GenerateSQLScripts.getAllRowsScript(this.tableName);
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectAndFetch(selectAllScript);

        try {
            Constructor<T> constructor = modelClass.getDeclaredConstructor();
            constructor.setAccessible(true);

            for(Map<String, Object> columnToValue: results) {
                T instance = constructor.newInstance();
                for(ColumnInfo col: columnInfos) {
                    String columnName = col.column().name();
                    Field field = col.field();

                    Object value = columnToValue.get(columnName);
                    field.set(instance, value);
                }

                rows.add(instance);
            }
        }

        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return rows;
    }
}
