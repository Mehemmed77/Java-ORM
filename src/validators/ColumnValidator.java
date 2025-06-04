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
}
