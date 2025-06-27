package core;

import metadata.ColumnInfo;

import java.util.List;

public class ModelLoader {
    public static void load(Class<? extends Model> clazz, List<ColumnInfo> columnInfos, ModelParser parser) {
        ModelCache.columnCache.put(clazz, columnInfos);
        ModelCache.pkUtilMap.put(clazz, parser.getPkUtil());
        ModelCache.updatedAtFieldMap.put(clazz, parser.doesHaveUpdatedAt());
        ModelCache.foreignKeyMap.put(clazz, parser.getForeignKeys());
    }
}
