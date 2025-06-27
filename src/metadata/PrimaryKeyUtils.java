package metadata;

import java.lang.reflect.Field;

public record PrimaryKeyUtils(Field pkField, String pkName, int pkIndex) {
}
