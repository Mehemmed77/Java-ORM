package core;

import annotations.Column;
import annotations.PrimaryKey;
import database.DatabaseManager;
import metadata.ColumnInfo;
import utils.GenerateSQLScripts;
import utils.TimeStampManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SaveMixin {
    protected static Object getPkValue(Model instance) {
        Field field = ModelInspector.getPkField(instance.getClass());

        Object pkValue = null;

        try {
            pkValue = field.get(instance);
        } catch (IllegalAccessException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return pkValue;
    }

    public static void save(Model instance) {
        Class<? extends Model> clazz = instance.getClass();
        String tableName = ModelInspector.resolveTableName(clazz);
        SchemaGuard.ensureTableExistsOrThrow(tableName);

        LinkedHashMap<String, Object> columnToValues = new LinkedHashMap<>();
        List<ColumnInfo> columnInfos = ModelInspector.getColumns(clazz);

        Object pkValue = getPkValue(instance);
        if (pkValue != null && (Integer) pkValue != 0) {
            UpdateMixin.update(instance, pkValue);
            return;
        }

        for (ColumnInfo info : columnInfos) {
            Column column = info.column();
            Field field = info.field();
            try {
                if (TimeStampManager.isManagedTimestamp(column)) {
                    TimeStampManager.validateNotManuallySet(instance, column, field);
                    if (TimeStampManager.isCreatedAt(column))
                        columnToValues.put(column.name(), TimeStampManager.now());
                    continue;
                }

                if (!(field.isAnnotationPresent(PrimaryKey.class))) {
                    columnToValues.put(column.name(), field.get(instance));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error saving: " + e.getMessage());
            }
        }

        String insertScript = GenerateSQLScripts.generateParameterizedInsert(tableName, new ArrayList<>(columnToValues.keySet()));
        System.out.println(insertScript);
        int generatedId = DatabaseManager.getInstance().executeSave(clazz, insertScript, new ArrayList<>(columnToValues.values()));

        Field pkField = ModelInspector.getPkField(clazz);
        try {
            pkField.set(instance, generatedId);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to assign generated ID: " + e.getMessage());
        }
    }
}
