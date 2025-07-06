package metadata;
import annotations.Column;
import annotations.ForeignKey;

import java.lang.reflect.Field;

public record ColumnInfo(Field field, Column column, ForeignKey foreignKey) {}
