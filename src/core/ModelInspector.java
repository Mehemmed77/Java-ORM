package core;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;
import customErrors.*;
import enums.ColumnType;
import metadata.ColumnInfo;
import utils.TimeStampManager;
import validators.ColumnValidator;
import java.lang.reflect.Field;
import java.util.*;

public class ModelInspector {
    private static final Map<Class<? extends  Model>, List<ColumnInfo>> cache = new HashMap<>();
    private static final Map<Class<? extends Model>, Field> pkFieldMap = new HashMap<>();
    private static final Map<Class<? extends Model>, Integer> pkIndexMap = new HashMap<>();
    private static final Map<Class<? extends Model>, Boolean> doesContainUpdatedAtField = new HashMap<>();

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

    public static Field getPkField(Class<? extends Model> clazz) {
        return pkFieldMap.getOrDefault(clazz, null);
    }

    public static int getPkIndex(Class<? extends  Model> clazz) {
        return pkIndexMap.getOrDefault(clazz,-1);
    }

    public static boolean doesTableHaveUpdatedAtField(Class<? extends Model> clazz) {
        return doesContainUpdatedAtField.getOrDefault(clazz, false);
    }

    public static List<ColumnInfo> getColumns(Class<? extends Model> clazz) {
        if (cache.containsKey(clazz)) return cache.get(clazz);

        int idx = -1;
        List<ColumnInfo> columnInfos = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();

        boolean primaryKeyExists = false;

        for (Field field: clazz.getDeclaredFields()) {

            if (field.isAnnotationPresent(Column.class)) {
                idx += 1;
                Column column = field.getAnnotation(Column.class);

                if(columnNames.contains(column.name())) {
                    throw new MultipleColumnsWithSameNameException("Duplicate column name detected: " + column.name() + " in model " + clazz.getSimpleName());
                }

                columnNames.add(column.name());

                ColumnValidator.validateColumnName(clazz.getSimpleName(), field.getName(), column);

                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    if (primaryKeyExists) throw new MultiplePrimaryKeyException("Model " + clazz.getSimpleName() +
                            " has more than one @PrimaryKey column. Only one is allowed.");

                    pkIndexMap.put(clazz, idx + 1);
                    pkFieldMap.put(clazz, field);
                    primaryKeyExists = true;
                }

                if (column.type() == ColumnType.TIMESTAMP) ColumnValidator.
                        validateTimeBasedColumns(clazz.getSimpleName(), field.getName(),column);

                if (TimeStampManager.isUpdatedAt(column)) doesContainUpdatedAtField.put(clazz, true);

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
