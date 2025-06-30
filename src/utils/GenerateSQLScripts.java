package utils;
import annotations.Column;
import annotations.ForeignKey;
import annotations.PrimaryKey;
import core.Model;
import core.ModelInspector;
import core.SchemaGuard;
import customErrors.AbsenceOfColumns;
import customErrors.ReferencedColumnMissingException;
import enums.ReferentialAction;
import metadata.ColumnInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class GenerateSQLScripts {
    public static String createTableScript(String tableName, List<ColumnInfo> columnInfos) {
        if (columnInfos.isEmpty()) throw new AbsenceOfColumns("Table must have at least 1 column.");

        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableName).append(" (");

        List<String> columnDefinitions = new ArrayList<>();
        List<String> foreignKeyConstraints = new ArrayList<>();

        for (ColumnInfo info : columnInfos) {
                columnDefinitions.add(buildColumnDefinition(info));
                if (info.foreignKey() != null) foreignKeyConstraints.add(buildForeignKeyConstraint(info));
        }

        sb.append(String.join(", ", columnDefinitions));

        if (!foreignKeyConstraints.isEmpty()) {
            sb.append(", ").append(String.join(", ", foreignKeyConstraints));
        }

        sb.append(")");
        System.out.println(sb);
        return sb.toString();
    }

    private static String buildForeignKeyConstraint(ColumnInfo info) {
        ForeignKey fk = info.foreignKey();
        String referencedTableName = ModelInspector.resolveTableName(fk.reference());

        // Ensure that referenced table is created.
        SchemaGuard.ensureTableExistsOrThrow(referencedTableName);

        String referencedTablePkName = ModelInspector.getPkUtil(fk.reference()).pkName();

        Column column = info.column();
        StringBuilder sb = new StringBuilder();

        sb.append("FOREIGN KEY (").append(column.name()).append(") ");
        sb.append("REFERENCES ").append(referencedTableName)
                .append("(").append(referencedTablePkName).append(")");

        if (fk.onDelete() != ReferentialAction.NO_ACTION) {
            sb.append(" ON DELETE ").append(fk.onDelete().name()).append(" ");
        }

        if (fk.onUpdate() != ReferentialAction.NO_ACTION) {
            sb.append(" ON UPDATE ").append(fk.onUpdate().name()).append(" ");
        }

        return sb.toString().trim();
    }

    private static String buildColumnDefinition(ColumnInfo info) {
        Column column = info.column();
        Field field = info.field();

        StringBuilder def = new StringBuilder(column.name()).append(" ");

        String type = column.type().name();
        if (type.equals("VARCHAR")) type = formatVarchar(column.length());
        def.append(type).append(" ");

        if (field.isAnnotationPresent(PrimaryKey.class)) {
            def.append("PRIMARY KEY AUTOINCREMENT");
        }

        if (!column.nullable()) def.append("NOT NULL ");
        if (column.unique()) def.append("UNIQUE ");

        return def.toString().trim();
    }

    public static String generateSelectScript(String tableName, String filterToSql) {
        String selectStatement = "SELECT * FROM " + tableName + " WHERE ";

        return selectStatement + filterToSql;
    }

    public static String generateAliasColumns(String aliasName, List<Column> columns) {
        return String.join(",", columns.stream().map(
                column -> aliasName + "." + column.name() + " AS " + aliasName + "_" + column.name()
        ).toList());
    }

    public static String generateJoinOnScript(
            String referencingTableAlias,  // the table that has the FK (e.g., "article")
            String fkColumnName,      // the FK column (e.g., "author_id")
            String referencedTable,        // the FK target table name (e.g., "Author")
            String referencedTableAlias,   // alias for the FK target (e.g., "author")
            String referencedPkColumn      // PK column in the referenced table (e.g., "id")
    ) {

        return "JOIN " + referencedTable + " AS " + referencedTableAlias +
                " ON " + referencingTableAlias + "." + fkColumnName + "=" + referencedTableAlias + "." + referencedPkColumn ;
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
