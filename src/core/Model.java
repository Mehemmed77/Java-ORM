package core;
import manager.QuerySet;
import java.lang.reflect.Field;

public abstract class Model {
    public static <T extends Model> QuerySet<T> objects(Class<T> modelClass){
        String tableName = ModelInspector.resolveTableName(modelClass);
        SchemaGuard.ensureTableExistsOrThrow(tableName);
        return new QuerySet<>(modelClass, tableName, ModelInspector.getColumns(modelClass));
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T set(String fieldName, Object value) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No such field: " + fieldName, e);
        }

        return (T) this;
    }

    public static void dropTable(Class<? extends Model> clazz) {
        TableMixin.dropTable(clazz);
    }

    public static void createTable(Class<? extends Model> clazz) {
        TableMixin.createTable(clazz);
    }

    public void update(Object pkValue) {
        UpdateMixin.update(this, pkValue);
    }

    public void save() {
        SaveMixin.save(this);
    }
}
