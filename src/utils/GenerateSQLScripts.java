package utils;

import annotations.Column;
import customErrors.AbsenceOfColumns;
import metadata.ColumnInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class GenerateSQLScripts {
    public static String createTable(String tableName, List<ColumnInfo> columnInfos) {
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

    public static String InsertInto(LinkedHashMap<String, Object> columnToValues, String tableName) {
        StringBuilder script = new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        List<String> keyList = columnToValues.keySet().stream().toList();

        String joinedKeys = String.join(",", keyList);

        script.append(joinedKeys).append(") VALUES (");

        for(int i = 0; i < keyList.size(); i++) {
            Object val = columnToValues.get(keyList.get(i));

            if (val instanceof String || val instanceof Character) {
                script.append(
                        String.format("'%s'", val)
                );
            }

            else if (val == null) {
                script.append("NULL");
            }

            else{
                script.append(val);
            }

            if (i != keyList.size() - 1) script.append(",");
        }

        script.append(")");

        return script.toString();
    }

    public static String tableExists(String table_name) {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table_name + "'";
    }

    private static String formatVarchar(int length) {
        return String.format("varchar(%d)", length);
    }
}
