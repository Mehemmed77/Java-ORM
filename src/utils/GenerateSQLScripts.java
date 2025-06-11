package utils;
import annotations.Column;
import customErrors.AbsenceOfColumns;
import metadata.ColumnInfo;
import java.util.List;

public class GenerateSQLScripts {
    public static String createTableScript(String tableName, List<ColumnInfo> columnInfos) {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableName).append(" (");

        if (columnInfos.isEmpty()) throw new AbsenceOfColumns("Table must have at least 1 column.");

        for(int i = 0; i < columnInfos.size(); i++) {
            Column column = columnInfos.get(i).column();

            sb.append(column.name()).append(" ");

            String type = column.type().name();
            if(type.equals("VARCHAR")) type = formatVarchar(column.length());

            sb.append(type).append(" ");

            if (column.primaryKey()) sb.append("PRIMARY KEY AUTOINCREMENT ");

            if (!column.nullable()) sb.append("NOT NULL ");
            if (column.unique()) sb.append("UNIQUE ");

            if (i != columnInfos.size() - 1) sb.append(",");
        }

        sb.append(")");

        return sb.toString();
    }

    public static String dropTableScript(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public static String generateParameterizedInsert(String tableName, List<String> keys) {
        String placeholders = String.join(",", keys.stream().map(k -> "?").toList());
        return "INSERT INTO " + tableName + " (" + String.join(",", keys) + ") VALUES (" + placeholders + ")";
    }

    public static String tableExistsScript(String table_name) {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table_name + "'" + " LIMIT 1";
    }

    private static String formatVarchar(int length) {
        return String.format("varchar(%d)", length);
    }
}
