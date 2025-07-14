package migrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import annotations.Column;
import annotations.ForeignKey;
import annotations.Table;
import core.Model;
import core.ModelInspector;

public class MigrationManager {

    /**
     * Entry point to load all model classes in the `models` package.
     */
    @SuppressWarnings("unchecked")
    public List<Class<? extends Model>> loadModels() {
        Reflections reflections = new Reflections("Models");

        Set<Class<?>> rawModels = reflections.getTypesAnnotatedWith(Table.class);

        List<Class<? extends Model>> models = rawModels.stream().filter(Model.class::isAssignableFrom)
        .map(clz -> (Class<? extends Model>) clz).collect(Collectors.toList());

        return models;
    }

     /**
     * Parse a single model class.
     * - Extract @Table, @Column, @PrimaryKey, @ForeignKey, etc.
     * - Build a representation you can serialize for migrations.
     */

     private String getMigrationID(Class<? extends Model> clazz) {
         try {
             Field field = clazz.getDeclaredField("MIGRATION_ID");
             return (String) field.get(null);
         } catch (NoSuchFieldException | IllegalAccessException e) {
             return null;
         }
     }

    public LinkedHashMap<String, Object> parseModel(Class<? extends Model> modelClass) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        String migrationID = getMigrationID(modelClass);

        map.put("tableName", ModelInspector.resolveTableName(modelClass));

        if(getMigrationID(modelClass) == null) map.put("MIGRATION_ID", UUID.randomUUID().toString());
        else map.put("MIGRATION_ID", migrationID);

        Field[] fields = modelClass.getDeclaredFields();
        List<Map<String, Object>> columns = new ArrayList<>();
        List<Map<String, Object>> getters = new ArrayList<>();
        
        for(Field field: fields) {
            if(field.isAnnotationPresent(Column.class)) columns.add(ColumnParser.parse(field));
            if(field.isAnnotationPresent(ForeignKey.class)) columns.add(ColumnParser.parseFK(field));

            if(!hasGetter(modelClass, field)) getters.add(makeGetter(field));
        }

        if (!hasToString(modelClass)) {
            map.put("toString", generateToString(modelClass.getSimpleName(), modelClass.getDeclaredFields()));
        }

        map.put("columns", columns);
        map.put("getters", getters);

        return map;
    }

    public String capitalizeString(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private Map<String, Object> makeGetter(Field field) {
        Map<String, Object> map = new HashMap<>();

        String typeName = field.getType().getName();

        map.put("getterName", "get" + capitalizeString(field.getName()));
        map.put("returnType", typeName);
        map.put("returnValue", "this." + field.getName());
        
        return map;
    }

    private boolean hasNoArgConstructor(Class<? extends Model> modelClass) {
        return Arrays.stream(modelClass.getDeclaredConstructors())
                .anyMatch(constructor -> constructor.getParameterCount() == 0);
    }

    private boolean hasToString(Class<? extends Model> modelClass) {
        List<Method> methods = Arrays.asList(modelClass.getDeclaredMethods());
        String methodNames = String.join(" ", methods.stream().map(Method::getName).toList());
        return methodNames.contains("toString");
    }

    public String generateToString(String className, Field[] fields) {
        String toStringBody = Arrays.stream(fields)
            .map(f -> {
                if(f.isAnnotationPresent(ForeignKey.class)) {
                    return f.getName() + "=" + "this.getRelated(" + f.getName() + ")";
                }
                return f.getName() + "=" + "this." + f.getName();
            })
            .collect(Collectors.joining(", "));

        return "return \"" + className + "{ " + toStringBody + "}";
    }

    public boolean hasGetter(Class<? extends Model> modelClass, Field field) {
        Method[] methods = modelClass.getMethods();

        for(Method method: methods) {
            if (method.getName().equals("get" + capitalizeString(field.getName()))) return true;
        }

        return false;
    }

}
