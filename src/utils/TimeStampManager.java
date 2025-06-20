package utils;
import annotations.Column;
import customErrors.ManualTimestampAssignmentException;
import enums.ColumnType;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class TimeStampManager {
    public static boolean isManagedTimestamp(Column column) {
        String name = column.name().toLowerCase();
        return column.type() == ColumnType.TIMESTAMP && (name.equals("created_at") || name.equals("updated_at"));
    }

    public static boolean isCreatedAt(String columnName) {
        return columnName.equalsIgnoreCase("created_at");
    }

    public static boolean isCreatedAt(Column column) {
        return column.name().equalsIgnoreCase("created_at");
    }

    public static boolean isUpdatedAt(String columnName) {
        return columnName.equalsIgnoreCase("updated_at");
    }

    public static boolean isUpdatedAt(Column column) {
        return column.name().equalsIgnoreCase("updated_at");
    }

    public static void validateNotManuallySet(Object model, Column column, Field field) {
        try {
            Object value = field.get(model);
            if (value != null) {
                throw new ManualTimestampAssignmentException("Field '" + column.name() + "' is managed by the ORM and cannot be set manually.");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + column.name() + "'", e);
        }
    }

    public static void validateNotManuallyUpdate(Set<String> keySet) {
        if (keySet.stream().filter(k ->
                !(isCreatedAt(k) || isUpdatedAt(k))
        ).toList().isEmpty()) throw new ManualTimestampAssignmentException("Timestamp fields cannot be updated.");
    }


    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }
}