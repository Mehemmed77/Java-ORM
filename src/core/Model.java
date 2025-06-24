package core;
import annotations.Column;
import annotations.PrimaryKey;
import manager.QuerySet;
import metadata.ColumnInfo;

import java.lang.reflect.Field;
import java.util.List;

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
            Column column = field.getAnnotation(Column.class);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No such field: " + fieldName, e);
        }

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    private <T extends Model> T privateSetSequentially(boolean includePk, Object... values) {
        List<ColumnInfo> columnInfos = ModelInspector.getColumns(this.getClass());

        int idx = 0;
        try{
            for(ColumnInfo columnInfo: columnInfos) {
                if (idx > values.length || values.length == 0) break;

                Field field = columnInfo.field();
                if (!includePk && field.isAnnotationPresent(PrimaryKey.class)) continue;

                field.setAccessible(true);
                field.set(this, values[idx]);
                idx++;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("No such field: ", e);
        }

        return (T) this;
    }

    public <T extends Model> T setSequentially(Object... values) {
        return privateSetSequentially(false, values);
    }

    public <T extends Model> T setSequentiallyWithoutIgnoringPK(Object... values) {
        return privateSetSequentially(true, values);
    }

    public static void dropTable(Class<? extends Model> clazz) {
        TableMixin.dropTable(clazz);
    }

    public static void createTable(Class<? extends Model> clazz) {
        TableMixin.createTable(clazz);
    }

    public void update() {
        UpdateMixin.update(this, SaveMixin.getPkValue(this));
    }

    public void save() {
        SaveMixin.save(this);
    }
}
