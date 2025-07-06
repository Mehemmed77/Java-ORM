package core;

import annotations.Table;
import customErrors.*;
import metadata.ColumnInfo;
import metadata.PrimaryKeyUtils;
import metadata.RelationMeta;

import java.util.*;

public abstract class ModelInspector {
    protected static void annotationPresent(Class<? extends Model> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) throw new AnnotationNotPresent("Table annotation must be implemented");
    }

    public static String resolveTableName(Class<? extends Model> clazz) {
        annotationPresent(clazz);
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table.name().isEmpty()) tableName = clazz.getSimpleName();
        else tableName = table.name();

        return tableName;
    }

    public static PrimaryKeyUtils getPkUtil(Class<? extends Model> clazz) {
        return ModelCache.pkUtilMap.getOrDefault(clazz, null);
    }

    public static boolean doesTableHaveUpdatedAtField(Class<? extends Model> clazz) {
        return ModelCache.updatedAtFieldMap.getOrDefault(clazz, false);
    }

    public static List<ColumnInfo> getColumns(Class<? extends Model> clazz) {
        if (ModelCache.columnCache.containsKey(clazz)) return ModelCache.columnCache.get(clazz);

        ModelParser parser = new ModelParser(clazz);
        List<ColumnInfo> columnInfos = parser.parse();

        ModelValidator.validateColumns(clazz, columnInfos);
        ModelLoader.load(clazz, columnInfos, parser);

        return columnInfos;
    }
}
