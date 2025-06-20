package validators;
import annotations.Column;

public class ColumnValidator {
    public static void validateColumnName(String className, String fieldName, Column column) {
        String columnName = column.name();

        if (columnName.contains(" ")) throw new IllegalArgumentException("Column name cannot contain empty space " +
                "Model: " + className + " column: " + fieldName);

        int firstCharacter = columnName.charAt(0) - '0';

        if (firstCharacter < 10) throw new IllegalArgumentException("Column name cannot start with a digit. " +
                "Model: " + className + "column: " + fieldName);
    }

    public static void validateTimeBasedColumns(String modelName, String fieldName, Column column) {
        String name = column.name().toLowerCase();

        if ( !(name.equalsIgnoreCase("created_at")
                || name.equalsIgnoreCase("updated_at"))) throw new IllegalArgumentException(
                        "Timestamps must have name of created_at or updated_at");

        if (column.autoIncrement()) {
            throw new IllegalArgumentException(modelName + "." + fieldName + ": autoIncrement cannot be true for " + name);
        }

        if (!column.nullable()) {
            throw new IllegalArgumentException(modelName + "." + fieldName + ": " + name + " must be nullable (default auto-filled)");
        }

        if (column.unique()) {
            throw new IllegalArgumentException(modelName + "." + fieldName + ": " + name + " cannot be unique");
        }

        if (column.length() != 255) {
            throw new IllegalArgumentException(modelName + "." + fieldName + ": " + name + " must not override length");
        }

        if (!column.defaultValue().equals("LOREM IPSUM")) {
            throw new IllegalArgumentException(modelName + "." + fieldName + ": " + name + " cannot have a custom defaultValue");
        }
    }
}
