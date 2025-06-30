package manager;

import core.Model;
import core.ModelCache;
import core.ModelInspector;
import filters.Filter;
import metadata.RelationMeta;

import java.lang.reflect.Field;
import java.util.List;

public class Related {
    private final Model instance;
    private final Class<? extends Model> instanceClass;
    private final Object pkValue;

    private Related(Model instance) {
        this.instance = instance;
        this.instanceClass = instance.getClass();
        this.pkValue = getPrimaryKeyValue(instance);
    }

    public static Related of(Model instance) {
        if (instance == null) throw new IllegalArgumentException();
        return new Related(instance);
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
