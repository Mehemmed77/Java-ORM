package metadata;

import java.util.List;
import java.util.Map;

public record SnapShot(String tableName, String MIGRATION_ID, String TOString, List<Map<String, Object>> columns, List<Map<String, Object>> getters) {
}
