package core;
import annotations.Table;
import annotations.Column;
import customErrors.AnnotationNotPresent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    private static List<Column> getColumns(Class<? extends Model> clazz) {
        List<Column> columns = new ArrayList<>();
        for (Field field: clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                columns.add(column);
            }
        }

        return columns;
    }

    // Entry point for creating table
    public static void createTable(Class<? extends Model> clazz) {
        String tableName = resolveTableName(clazz);
        getColumns(clazz);
    }
}
