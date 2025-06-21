package core;
import annotations.Column;
import database.DatabaseManager;
import manager.QuerySet;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import utils.TimeStampManager;
import java.lang.reflect.Field;
import java.util.*;

public abstract class Model {
    public static <T extends Model> QuerySet<T> objects(Class<T> modelClass){
        String tableName = ModelInspector.resolveTableName(modelClass);
        SchemaGuard.ensureTableExistsOrThrow(tableName);
        return new QuerySet<>(modelClass, tableName, ModelInspector.getColumns(modelClass));
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T set(String fieldName, Object value) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No such field: " + fieldName, e);
        }

        return (T) this;
    }

    public static void dropTable(Class<? extends Model> clazz) {
        String tableName = ModelInspector.resolveTableName(clazz);

        String dropTableScript = GenerateSQLScripts.dropTableScript(tableName);
        DatabaseManager.getInstance().executeUpdate(dropTableScript, null);
    }

    // Entry point for creating table
    public static void createTable(Class<? extends Model> clazz) {
        String tableName = ModelInspector.resolveTableName(clazz);
        SchemaGuard.ensureEmptyConstructorExistsOrThrow(clazz);
        SchemaGuard.ensureTableNotExistsOrThrow(tableName);

        String tableCreationScript = GenerateSQLScripts.createTableScript(tableName, ModelInspector.getColumns(clazz));

        DatabaseManager.getInstance().executeUpdate(tableCreationScript, null);
    }

    public void save() {
        String tableName = ModelInspector.resolveTableName(this.getClass());
        SchemaGuard.ensureTableExistsOrThrow(tableName);

        LinkedHashMap<String, Object> columnToValues = new LinkedHashMap<>();
        List<ColumnInfo> columnInfos = ModelInspector.getColumns(this.getClass());

        for (ColumnInfo i : columnInfos) {
            Column column = i.column();
            Field field = i.field();
            String colName = column.name().toLowerCase();

            try {
                if (TimeStampManager.isManagedTimestamp(column)) {
                    TimeStampManager.validateNotManuallySet(this, column, field);

                    // Only set created_at on insert
                    if (TimeStampManager.isCreatedAt(column)) columnToValues.put(colName, TimeStampManager.now());

                    // Skip updated_at on insert
                    continue;
                }

                if (!(column.primaryKey() && column.autoIncrement())) {
                    columnToValues.put(column.name(), field.get(this));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error processing field '" + column.name() + "': " + e.getMessage(), e);
            }
        }

        List<String> keys = columnToValues.keySet().stream().toList();
        List<Object> values = columnToValues.values().stream().toList();

        String script = GenerateSQLScripts.generateParameterizedInsert(tableName, keys);

        int id = DatabaseManager.getInstance().executeSave(this.getClass(), script, values);

        Field field = ModelInspector.getPkField(this.getClass());

        try {
            field.set(this, id);
        } catch (IllegalAccessException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}
