package core;
import annotations.Table;
import annotations.Column;
import customErrors.*;
import database.DatabaseManager;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import validators.ColumnValidator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class Model {
    // Only checks annotation existence
    private static void annotationPresent(Class<? extends Model> clazz) throws AnnotationNotPresent {
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

    // if table does not exist then throw custom error.
    private void throwIfTableDoesNotExist() {
        String tableName = resolveTableName(this.getClass());
        String tableExistsScript = GenerateSQLScripts.tableExists(tableName);

        if (!DatabaseManager.getInstance().executeCommandAndReturn(tableExistsScript)){
            throw new MissingTableException("Table named " + tableName + " does not exist");
        }
    }

    private static void throwIfTableAlreadyExists(String tableName) {
        String tableExistsScript = GenerateSQLScripts.tableExists(tableName);

        if (DatabaseManager.getInstance().executeCommandAndReturn(tableExistsScript)){
            throw new TableAlreadyExistsException("Table named " + tableName + " already exists");
        }
    }

    private static List<ColumnInfo> getColumns(Class<? extends Model> clazz) {
        List<ColumnInfo> columnInfos = new ArrayList<>();

        boolean primaryKeyExists = false;

        for (Field field: clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);

                if (column.primaryKey()) {
                    // throw error if there is more than 1 primary key column.
                    if (primaryKeyExists) throw new MultiplePrimaryKeyException("Model " + clazz.getSimpleName() +
                            " has more than one @PrimaryKey column. Only one is allowed.");

                    primaryKeyExists = true;
                }

                ColumnValidator.validateColumnName(clazz.getSimpleName(),field.getName(), column);

                columnInfos.add(new ColumnInfo(field, column));
            }
        }

        // throw error if there is no primary key present.
        if (!primaryKeyExists) throw new MissingPrimaryKeyException("Model named " + clazz.getSimpleName() + " misses @PrimaryKey column.");

        return columnInfos;
    }

    // Entry point for creating table
    public static void createTable(Class<? extends Model> clazz) {
        String tableName = resolveTableName(clazz);

        throwIfTableAlreadyExists(tableName);

        String tableCreationScript = GenerateSQLScripts.createTable(tableName, getColumns(clazz));

        DatabaseManager databaseManager = DatabaseManager.getInstance();

        if (DatabaseManager.isConnected()) {
            databaseManager.executeCommand(tableCreationScript);
        }
    }

    public void save() {
        throwIfTableDoesNotExist();
        String tableName = resolveTableName(this.getClass());

        LinkedHashMap<String, Object> columnToValues = new LinkedHashMap<>();
        List<ColumnInfo> columnInfos = getColumns(this.getClass());

        for (ColumnInfo i : columnInfos) {
            try {
                Column column = i.column();
                Field field = i.field();

                if (!column.primaryKey()) {
                    field.setAccessible(true);
                    columnToValues.put(column.name(), field.get(this));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // If there is no columns throw custom error.
        if (columnToValues.isEmpty()) {
            throw new MissingColumnsException("No columns to insert in the table named " + tableName);
        }

        String script = GenerateSQLScripts.InsertInto(columnToValues, tableName);
        DatabaseManager.getInstance().executePreparedStatement(script);
    }
}
