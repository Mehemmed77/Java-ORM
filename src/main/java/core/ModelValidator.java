package core;

import customErrors.MultipleColumnsWithSameNameException;
import metadata.ColumnInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelValidator {
    public static void validateColumns(Class<? extends Model> clazz, List<ColumnInfo> columns) {
        Set<String> names = new HashSet<>();
        for (ColumnInfo column : columns) {
            String name = column.column().name();
            if (names.contains(name)) {
                throw new MultipleColumnsWithSameNameException("Duplicate column name in model " + clazz.getSimpleName() + ": " + name);
            }
            names.add(name);
        }
    }
}
