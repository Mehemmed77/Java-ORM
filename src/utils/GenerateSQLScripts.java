package utils;
import annotations.Column;
import customErrors.AbsenceOfColumns;
import metadata.ColumnInfo;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

    public static String generateSelectScript(String tableName, String filterToSql) {
        String selectStatement = "SELECT * FROM " + tableName + " WHERE ";

        return selectStatement + filterToSql;
    }

    public static String generateExistsScript(String tableName, String filterToSql) {
        return "SELECT 1 FROM " + tableName + " WHERE " + filterToSql + " LIMIT 1";
    }

    public static String generateCountAllScript(String tableName) {
        return "SELECT COUNT(*) FROM " + tableName;
    }

    public static String generateCountScript(String tableName, String filterToSql) {
        return "SELECT COUNT(*) FROM " + tableName + " WHERE " + filterToSql;
    }

    public static String deleteALlScript(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public static String generateDeleteScript(String tableName, String filterToSql) {
        return "DELETE FROM " + tableName + " WHERE " + filterToSql;
    }

    public static String generateUpdateScript(String tableName, Set<String> keys, LinkedHashMap<String, Object> mappedValues, String filterToSql) {
        String placeholders = String.join(",", keys.stream().map(
                key -> mappedValues.get(key) != null ? key + " = ?" : key + " = NULL")
                .toList());
        return "UPDATE " + tableName + " SET " + placeholders + " WHERE " + filterToSql;
    }

    public static String getAllRowsScript(String tableName) {
        return "SELECT * FROM " + tableName;
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
