package metadata;
import annotations.Column;
import java.lang.reflect.Field;

public record ColumnInfo(Field field, Column column) {}
