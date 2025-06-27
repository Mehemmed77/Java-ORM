package core;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;
import filters.Filter;
import metadata.ColumnInfo;
import utils.TimeStampManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UpdateMixin {

    @SuppressWarnings("unchecked")
    public static <T> void update(Model instance, Object pkValue) {
        Class<? extends Model> clazz = instance.getClass();
        Field pkField = ModelInspector.getPkUtil(clazz).pkField();

        String pkFieldName = pkField.getAnnotation(Column.class).name();

        T selfTyped = (T) Model.objects(clazz).
                get(Filter.eq(pkFieldName, pkValue));

        Map<String, Object> changedValues = new HashMap<>();

        try {
            for(ColumnInfo columnInfo: ModelInspector.getColumns(clazz)) {
                Field field = columnInfo.field();
                Column column = columnInfo.column();

                if (TimeStampManager.isManagedTimestamp(column)) continue;

                Object oldValue = field.get(selfTyped);
                Object newValue = field.get(instance);

                if((oldValue == null && newValue != null) ||
                        (oldValue != null && !oldValue.equals(newValue))) {
                    String colName = column.name();
                    changedValues.put(colName, newValue);
                }
            }

            if (changedValues.isEmpty()) {
                System.out.println("No updates detected.");
                return;
            }


            Filter filter = Filter.eq(pkFieldName, pkValue);
            Model.objects(clazz).update(changedValues, filter);

            System.out.println("Updated values are: " + changedValues);

        } catch (IllegalAccessException e) {
            System.out.println("Some error occurred: " + e.getMessage());
        }
    }
}
