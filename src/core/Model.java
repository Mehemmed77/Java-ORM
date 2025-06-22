package core;
import annotations.Column;
import annotations.Table;
import database.DatabaseManager;
import filters.Filter;
import manager.QuerySet;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import utils.TimeStampManager;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
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

    private Object checkIfInstanceExists() {
        Field field = ModelInspector.getPkField(this.getClass());

        Object pkValue = null;

        try {
            pkValue = field.get(this);
        } catch (IllegalAccessException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return pkValue;
    }

    @SuppressWarnings("unchecked")
    public <T> void update(Object pkValue) {
        String pkFieldName = this.getClass().getAnnotation(Table.class).primaryKeyName();

        T selfTyped = (T) Model.objects(this.getClass()).
                get(Filter.eq(pkFieldName, pkValue));

        Map<String, Object> changedValues = new HashMap<>();

        try {
            for(ColumnInfo columnInfo: ModelInspector.getColumns(this.getClass())) {
                Field field = columnInfo.field();
                Column column = columnInfo.column();

                if (TimeStampManager.isManagedTimestamp(column)) continue;

                Object oldValue = field.get(selfTyped);
                Object newValue = field.get(this);

                if((oldValue == null && newValue != null) ||
                        (oldValue != null && !oldValue.equals(newValue))) {
                    String colName = column.name();
                    changedValues.put(colName, newValue);
                }
            }

            if (changedValues.isEmpty()) {
                System.out.println("No updates detected.");
                return;
            }


            Filter filter = Filter.eq(pkFieldName, pkValue);
            Model.objects(this.getClass()).update(changedValues, filter);

            System.out.println("Updated values are: " + changedValues);

        } catch (IllegalAccessException e) {
            System.out.println("Some error occurred: " + e.getMessage());
        }
    }

    public void save() {
        String tableName = ModelInspector.resolveTableName(this.getClass());
        SchemaGuard.ensureTableExistsOrThrow(tableName);

        LinkedHashMap<String, Object> columnToValues = new LinkedHashMap<>();
        List<ColumnInfo> columnInfos = ModelInspector.getColumns(this.getClass());

        Object pkValue = checkIfInstanceExists();
        if (pkValue != null) {
            update(pkValue);
            return;
        }

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
