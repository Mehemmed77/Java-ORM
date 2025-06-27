package core;

import metadata.ColumnInfo;
import metadata.PrimaryKeyUtils;
import metadata.ReverseRelationMap;

import java.lang.reflect.Field;
import java.util.*;

public class ModelCache {
    public static final Map<Class<? extends Model>, List<ColumnInfo>> columnCache = new HashMap<>();
    public static final Map<Class<? extends Model>, PrimaryKeyUtils> pkUtilMap = new HashMap<>();
    public static final Map<Class<? extends Model>, List<ColumnInfo>> foreignKeyMap = new HashMap<>();
    public static final Map<Class<? extends Model>, List<ReverseRelationMap>> relatedModels = new HashMap<>();
    public static final Map<Class<? extends Model>, Boolean> updatedAtFieldMap = new HashMap<>();
}