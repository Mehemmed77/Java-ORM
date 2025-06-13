package core;
import annotations.Table;
import annotations.Column;
import customErrors.*;
import database.DatabaseManager;
import manager.QuerySet;
import metadata.ColumnInfo;
import customErrors.MultipleColumnsWithSameNameException;
import utils.GenerateSQLScripts;
import validators.ColumnValidator;
import java.lang.reflect.Field;
import java.util.*;

public abstract class Model {
    public static <T extends Model> QuerySet<T> objects(Class<T> modelClass){
        String tableName = resolveTableName(modelClass);
        ensureTableExistsOrThrow(tableName);
        return new QuerySet<>(modelClass, tableName, getColumns(modelClass));
    }

    // Only checks annotation existence
    private static void annotationPresent(Class<? extends Model> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) throw new AnnotationNotPresent("Table annotation must be implemented");
    }

    // Extracts the table name (class name fallback)
    private static String resolveTableName(Class<? extends Model> clazz) {
        annotationPresent(clazz);
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table.name().isEmpty()) tableName = clazz.getSimpleName();
        else tableName = table.name();

        return tableName;
    }

    // Helper class that returns if table exists or not.
    private static boolean doesTableExist(String tableName) {
        String script = GenerateSQLScripts.tableExistsScript(tableName);
        return DatabaseManager.getInstance().executeCommandAndReturn(script);
    }

    // Throws if table does not exist.
    private static void ensureTableExistsOrThrow(String tableName) {
        if (!doesTableExist(tableName)) throw new MissingTableException("Table named " + tableName + " does not exist");
    }

    // Throws if table exists.
    private static void ensureTableNotExistsOrThrow(String tableName) {
        if (doesTableExist(tableName)) throw new TableAlreadyExistsException("Table named " + tableName + " already exists");
    }

    public static List<ColumnInfo> getColumns(Class<? extends Model> clazz) {
        List<ColumnInfo> columnInfos = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();

        boolean primaryKeyExists = false;

        for (Field field: clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);

                if(columnNames.contains(column.name())) {
                    throw new MultipleColumnsWithSameNameException("Duplicate column name detected: " + column.name() + " in model " + clazz.getSimpleName());
                }
                columnNames.add(column.name());

                if (column.primaryKey()) {
                    // throw error if there is more than 1 primary key column.
                    if (primaryKeyExists) throw new MultiplePrimaryKeyException("Model " + clazz.getSimpleName() +
                            " has more than one @PrimaryKey column. Only one is allowed.");

                    primaryKeyExists = true;
                }

                ColumnValidator.validateColumnName(clazz.getSimpleName(),field.getName(), column);

                field.setAccessible(true);
                columnInfos.add(new ColumnInfo(field, column));
            }
        }

        // throw error if there is no primary key present.
        if (!primaryKeyExists) throw new MissingPrimaryKeyException("Model named " + clazz.getSimpleName() + " misses @PrimaryKey column.");

        // If there is no columns throw custom error.
        if (columnInfos.isEmpty()) {
            throw new MissingColumnsException("No columns to insert in the model named " + clazz.getSimpleName());
        }

        return columnInfos;
    }

    public static void dropTable(Class<? extends Model> clazz) {
        String tableName = resolveTableName(clazz);

        String dropTableScript = GenerateSQLScripts.dropTableScript(tableName);
        DatabaseManager.getInstance().executeCommand(dropTableScript);
    }

    // Entry point for creating table
    public static void createTable(Class<? extends Model> clazz) {
        String tableName = resolveTableName(clazz);
        Model.ensureTableNotExistsOrThrow(tableName);

        String tableCreationScript = GenerateSQLScripts.createTableScript(tableName, getColumns(clazz));

        DatabaseManager databaseManager = DatabaseManager.getInstance();

        if (DatabaseManager.isConnected()) {
            databaseManager.executeCommand(tableCreationScript);
        }
    }

    public void save() {
        String tableName = resolveTableName(this.getClass());
        Model.ensureTableExistsOrThrow(tableName);

        LinkedHashMap<String, Object> columnToValues = new LinkedHashMap<>();
        List<ColumnInfo> columnInfos = getColumns(this.getClass());

        for (ColumnInfo i : columnInfos) {
            try {
                Column column = i.column();
                Field field = i.field();

                if (!(column.primaryKey() && column.autoIncrement())) {
                    columnToValues.put(column.name(), field.get(this));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> keys = columnToValues.keySet().stream().toList();
        List<Object> values = columnToValues.values().stream().toList();

        String script = GenerateSQLScripts.generateParameterizedInsert(tableName, keys);
        DatabaseManager.getInstance().executeInsert(values, script);
    }
}
