package utils;

import annotations.Column;
import customErrors.AbsenceOfColumns;
import metadata.ColumnInfo;

import java.util.List;

public class GenerateSQLScripts {
    public static String generateSQLScriptFromTableAndColumns(String tableName, List<ColumnInfo> columnInfos) {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableName + " (");

        if (columnInfos.size() == 0) throw new AbsenceOfColumns("Table must have at least 1 column.");

        for(int i = 0; i < columnInfos.size(); i++) {
            Column column = columnInfos.get(i).column();

            sb.append(column.name()).append(" ");

            String type = column.type().name();
            if(type.equals("VARCHAR")) type = formatVarchar(column.length());

            sb.append(type).append(" ");

            if (column.primaryKey()) sb.append("PRIMARY KEY ");

            if (!column.nullable()) sb.append("NOT NULL ");
            if (column.unique()) sb.append("UNIQUE ");

            if (i != columnInfos.size() - 1) sb.append(",");
        }

        sb.append(")");

        return sb.toString();
    }

    private static String formatVarchar(int length) {
        return String.format("varchar(%d)", length);
    }
}
