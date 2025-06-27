package manager;

import annotations.ForeignKey;
import core.Model;
import core.ModelInspector;
import filters.Filter;
import metadata.ReverseRelationMap;
import utils.GenerateSQLScripts;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Related {
    private final Model instance;
    private final Class<? extends Model> instanceClass;
    private final Object pkValue;

    private static final Map<Class<? extends Model>, List<RelationMeta>> reverseMap = new HashMap<>();

    private Related(Model instance) {
        this.instance = instance;
        this.instanceClass = instance.getClass();
        this.pkValue = getPrimaryKeyValue(instance);
    }

    public static Related of(Model instance) {
        if (instance == null) throw new IllegalArgumentException();
        return new Related(instance);
    }

    public <T extends Model> List<T> get(Class<T> relatedModelClass) {
        // Fetch related objects of given class
        // Check reverseMap for matching entries, run query
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Model> get(String relatedName) {
        List<ReverseRelationMap> relatedModels = ModelInspector.getRelatedModelsOfClass(instanceClass);

        try {
            for(ReverseRelationMap relationMap: relatedModels) {
                if (relatedName.equals(relationMap.relatedName())) {
                    // REFERENCING MODEL IS THE MODEL THAT HAS THE PRIMARY KEY!
                    Field pkField = ModelInspector.getPkUtil(instanceClass).pkField();
                    Object id = pkField.get(instance);
                    String fkColumnName = relationMap.info().column().name();

                    Filter filter = Filter.eq(fkColumnName, id);
                    return (List<Model>) Model.objects(relationMap.info().foreignKey().reference()).filter(filter);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        throw new IllegalArgumentException("Related name '" + relatedName + "' is not valid for " + instanceClass.getSimpleName());
    }

    // INTERNAL: Register reverse relations during schema inspection
    public static void registerReverseRelation(Class<? extends Model> referencingModel, Field fkField, ForeignKey fk) {
        // Add metadata about reverse link
    }

    private Object getPrimaryKeyValue(Model model) {
        try {
            Field pkField = ModelInspector.getPkUtil(instanceClass).pkField();
            return pkField.get(model);
        }

        catch (IllegalAccessException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return null;
    }
}
