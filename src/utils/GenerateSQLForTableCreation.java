package utils;

import annotations.Column;
import customErrors.AbsenceOfColumns;

import java.util.List;

public class GenerateSQLForTableCreation {
    public static String generateSQLScriptFromTableAndColumns(String tableName, List<Column> columns) {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableName + " (");

        if (columns.size() == 0) throw new AbsenceOfColumns("Table must have at least 1 column.");

        for(Column column: columns) {
            sb.append(column.name() + " ");

            String type = column.type().name();
            if(type.equals("VARCHAR")) type = formatVarchar(column.length());

            sb.append(type + " ");
        }

        return sb.toString();
    }

    private static String formatVarchar(int length) {
        return String.format("varchar(%d)", length);
    }
}
