package validators;
import annotations.Column;
import metadata.ColumnInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ValueValidator {
    public static void validate(Map<String, Object> valuesToBeUpdated, List<ColumnInfo> columnInfos) {
        if (valuesToBeUpdated.isEmpty()) {
            throw new IllegalArgumentException("No fields provided for update.");
        }

        // Map of column names to their metadata for quick access
        Map<String, Column> columnMap = new HashMap<>();
        for (ColumnInfo info : columnInfos) {
            columnMap.put(info.column().name(), info.column());
        }

        for (Map.Entry<String, Object> entry : valuesToBeUpdated.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Column column = columnMap.get(key);
            if (column == null) {
                throw new IllegalArgumentException("Column '" + key + "' does not exist in the table.");
            }

            if (!column.nullable() && value == null) {
                throw new IllegalArgumentException("Column '" + key + "' is not nullable but received null.");
            }
        }
    }
}
