package core;

import annotations.Column;
import annotations.Table;
import customErrors.*;
import enums.ColumnType;
import metadata.ColumnInfo;
import validators.ColumnValidator;
import java.lang.reflect.Field;
import java.util.*;

public class ModelInspector {
    private static final Map<Class<? extends  Model>, List<ColumnInfo>> cache = new HashMap<>();

    protected static void annotationPresent(Class<? extends Model> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) throw new AnnotationNotPresent("Table annotation must be implemented");
    }

    protected static String resolveTableName(Class<? extends Model> clazz) {
        annotationPresent(clazz);
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table.name().isEmpty()) tableName = clazz.getSimpleName();
        else tableName = table.name();

        return tableName;
    }

    public static List<ColumnInfo> getColumns(Class<? extends Model> clazz) {
        if (cache.containsKey(clazz)) return cache.get(clazz);

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

                ColumnValidator.validateColumnName(clazz.getSimpleName(), field.getName(), column);

                if (column.type() == ColumnType.TIMESTAMP) ColumnValidator.
                        validateTimeBasedColumns(clazz.getSimpleName(), field.getName(),column);

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

        cache.put(clazz, columnInfos);

        return columnInfos;
    }
}
