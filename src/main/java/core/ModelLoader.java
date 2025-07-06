package core;

import metadata.ColumnInfo;
import metadata.RelationMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelLoader {
    public static void load(Class<? extends Model> clazz, List<ColumnInfo> columnInfos, ModelParser parser) {
        ModelCache.columnCache.put(clazz, columnInfos);
        ModelCache.pkUtilMap.put(clazz, parser.getPkUtil());
        ModelCache.updatedAtFieldMap.put(clazz, parser.doesHaveUpdatedAt());
        ModelCache.foreignKeyMap.put(clazz, parser.getForeignKeys());

        for (Map.Entry<Class<? extends Model>, List<RelationMeta>> entry : parser.getReverseRelations().entrySet()) {
            ModelCache.relatedModels.putIfAbsent(entry.getKey(), new ArrayList<>());
            ModelCache.relatedModels.get(entry.getKey()).addAll(entry.getValue());
        }
    }
}
