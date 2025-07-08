package migrationManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import annotations.Column;
import annotations.ForeignKey;
import annotations.PrimaryKey;
import enums.ColumnType;

public class ColumnParser {
    public static Map<String, Object> parse(Field field) {
        Map<String, Object> map = new HashMap<>();
        
        Column column = field.getAnnotation(Column.class);
        map.put("columnName", column.name());
        map.put("columnType", column.type());
        
        if(field.isAnnotationPresent(PrimaryKey.class)) map.put("primaryKey", true);
        else{
            map.put("primaryKey", false);
            map.put("nullable", column.nullable());
            map.put("unique", column.unique());
            if (column.type().equals(ColumnType.VARCHAR)) map.put("length", column.length());
            map.put("defaultValue", column.defaultValue());
        }
        
        return map;
    }
    
    public static Map<String, Object> parseFK(Field field) {
        Map<String, Object> map = new HashMap<>();

        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        map.put("relatedName", foreignKey.relatedName());
        map.put("referencedModel", foreignKey.reference());
        map.put("onDelete", foreignKey.onDelete());
        map.put("onUpdate", foreignKey.onUpdate());
        map.put("nullable", foreignKey.nullable());

        return map;
    }
}
