package manager;

import core.Model;
import core.ModelCache;
import core.ModelInspector;
import filters.Filter;
import metadata.RelationMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Related {
    private final Model instance;
    private final Class<? extends Model> instanceClass;
    private final Object pkValue;
    public static final Map<Class<? extends Model>, Map<String, List<Model>>> cache = new HashMap<>();

    private Related(Model instance) {
        this.instance = instance;
        this.instanceClass = instance.getClass();
        this.pkValue = getPrimaryKeyValue(instance);
    }

    public static Related of(Model instance) {
        if (instance == null) throw new IllegalArgumentException();
        return new Related(instance);
    }

    public static Map<String, Object> filterRows(Class<? extends Model> clazz, Map<String, Object> data) {
        String tableName = ModelInspector.resolveTableName(clazz).toLowerCase();

        Map<String, Object> result = new HashMap<>();
        for(String key: data.keySet()){
            if(key.startsWith(tableName + "_")) result.put(key.substring(tableName.length() + 1), data.get(key));
        }

        return result;
    }

    public static void fillCache(List<Map<String, Object>> rows,
                                 String fkName,
                                 Class<? extends Model> referencedModel,
                                 Class<? extends Model> referencingModel) {

        List<Model> result = new ArrayList<>();

        if (!cache.containsKey(referencedModel)) cache.put(referencedModel, new HashMap<>());

        if(!cache.get(referencedModel).containsKey(fkName)) cache.get(referencedModel).put(fkName, new ArrayList<>());

        for (Map<String, Object> row: rows) {
            try {
                Model referencedModelInstance = Model.objects(referencedModel).
                        hydrateSingleInstance( filterRows(referencedModel, row) );

                result.add(referencedModelInstance);

                Map<String, Object> referencingModelMap = filterRows(referencingModel, row);
                referencingModelMap.put(fkName, referencedModelInstance);

                Model referencingModelInstance = Model.objects(referencingModel).
                        hydrateSingleInstance(referencingModelMap);



            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public <T extends Model> List<T> get(Class<T> relatedModelClass) {
        List<RelationMeta> relations = ModelCache.relatedModels.getOrDefault(instanceClass, List.of());

        if (relations.isEmpty()) throw new IllegalArgumentException(instanceClass.getSimpleName() + " has no relation with any model.");

        try {
            for(RelationMeta relation: relations) {
                if (relatedModelClass.equals(relation.referencingModel())) {
                    Filter filter = Filter.eq(relation.referencingFieldName(), pkValue);
                    return (List<T>) Model.objects(relation.referencingModel()).filter(filter);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to fetch related objects: " + e.getMessage(), e);
        }

        throw new IllegalArgumentException("Related model '" + relatedModelClass.getSimpleName() +
                "' is not valid for " + instanceClass.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> get(String relatedName) {
        List<RelationMeta> relations = ModelCache.relatedModels.getOrDefault(instanceClass, List.of());

        if (relations.isEmpty()) throw new IllegalArgumentException(instanceClass.getSimpleName() + " has no relation with any model.");

        try {
            for(RelationMeta relation: relations) {
                if (relatedName.equals(relation.relatedName())) {
                    Filter filter = Filter.eq(relation.referencingFieldName(), pkValue);
                    return (List<T>) Model.objects(relation.referencingModel()).filter(filter);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch related objects: " + e.getMessage(), e);
        }

        throw new IllegalArgumentException("Related name '" + relatedName + "' is not valid for " + instanceClass.getSimpleName());
    }

    private Object getPrimaryKeyValue(Model model) {
        try {
            Field pkField = ModelInspector.getPkUtil(instanceClass).pkField();
            Object id = pkField.get(model);
            if (id == null || (Integer) id == 0) throw new IllegalArgumentException("Model instance hasn't been saved.");

            return id;
        }

        catch (IllegalAccessException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return null;
    }
}
