package core;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.PrimaryKey;
import filters.Filter;
import manager.QuerySet;
import metadata.ColumnInfo;

public abstract class Model {
    public Map<String, Object> proxyMap = new HashMap<>();

    public static <T extends Model> QuerySet<T> objects (Class<T> modelClass){
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
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No such field: " + fieldName, e);
        }

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    private <T extends Model> T privateSetSequentially(boolean includePk, Object... values) {
        List<ColumnInfo> columnInfos = ModelInspector.getColumns(this.getClass());

        if (!includePk) {
            columnInfos = columnInfos.stream().
                filter(info -> !info.field().isAnnotationPresent(PrimaryKey.class)).toList();
        }

        try{

            for(int i = 0; i < values.length; i++) {
                 ColumnInfo info = columnInfos.get(i);

                 Field field = info.field();

                 field.setAccessible(true);
                 field.set(this, values[i]);
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

    @SuppressWarnings("unchecked")
    public <T extends Model> T getRelated(String fieldName) {
        try {
            Field fkField = this.getClass().getDeclaredField(fieldName);
            fkField.setAccessible(true);
            T value = (T) fkField.get(this);

            if (value == null) {
                Object fkId = proxyMap.get(fieldName);
                if (fkId != null) {

                    if (!Model.class.isAssignableFrom(fkField.getType())) {
                        throw new IllegalArgumentException("Field " + fieldName + " is not a related model");
                    }

                    Class<T> type = (Class<T>) fkField.getType();

                    List<T> related = Model.objects(type)
                            .filter(Filter.eq(
                                    ModelCache.pkUtilMap.get(type).pkName(),
                                    fkId
                            ));

                    if (!related.isEmpty()) {
                        value = related.getFirst();
                        fkField.set(this, value); // cache it
                    }
                }
            }
            return value;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
