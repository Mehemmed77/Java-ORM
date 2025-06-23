package core;

import database.DatabaseManager;
import utils.GenerateSQLScripts;

public class TableMixin {
    public static void dropTable(Class<? extends Model> clazz) {
        String tableName = ModelInspector.resolveTableName(clazz);

        String dropTableScript = GenerateSQLScripts.dropTableScript(tableName);
        DatabaseManager.getInstance().executeUpdate(dropTableScript, null);
    }

    // Entry point for creating table
    public static void createTable(Class<? extends Model> clazz) {
        String tableName = ModelInspector.resolveTableName(clazz);
        SchemaGuard.ensureEmptyConstructorExistsOrThrow(clazz);
        SchemaGuard.ensureTableNotExistsOrThrow(tableName);

        String tableCreationScript = GenerateSQLScripts.createTableScript(tableName, ModelInspector.getColumns(clazz));

        DatabaseManager.getInstance().executeUpdate(tableCreationScript, null);
    }
}
