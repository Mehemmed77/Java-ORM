package core;
import annotations.Table;
import customErrors.AnnotationNotPresent;

public abstract class Model {
    private static void annotationPresent(Class<?> clazz) throws AnnotationNotPresent {
        if (!clazz.isAnnotationPresent(Table.class)) throw new AnnotationNotPresent("Table annotation must be implemented");
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table.name().isEmpty()) tableName = clazz.getName();
        else tableName = table.name();
    }

    public static void createTable(Class<? extends Model> clazz) {

    }
}
