package manager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import annotations.Column;
import annotations.ForeignKey;
import core.Model;
import core.ModelCache;
import core.ModelInspector;
import customErrors.GetReturnedLessThanOneRowException;
import customErrors.GetReturnedMoreThanOneRowException;
import customErrors.NullFilterException;
import database.DatabaseManager;
import filters.Filter;
import metadata.ColumnInfo;
import metadata.RelationMeta;
import utils.GenerateSQLScripts;
import utils.TimeStampManager;
import validators.ValueValidator;

public class QuerySet<T extends Model> {
    private final String tableName;
    private final Class<T> modelClass;
    private final List<ColumnInfo> columnInfos;
    private final Constructor<T> constructor;

    public QuerySet(Class<T> clazz, String tableName, List<ColumnInfo> columnInfos) {
        try{
            modelClass = clazz;
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.tableName = tableName;
            this.columnInfos = columnInfos;

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public T hydrateSingleInstance(Map<String, Object> data) {
        try {
            T instance = constructor.newInstance();

            for(ColumnInfo col : columnInfos) {
                String columnName = col.column().name();
                Field field = col.field();

                Object value = data.get(columnName);

                if (field.isAnnotationPresent(ForeignKey.class)) {
                    if (value instanceof Model) field.set(instance, value);

                    else{
                        field.set(instance, null);
                        instance.proxyMap.put(field.getName(), value);
                    }
                    continue;
                }

                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Class<T>> findFieldRecursively(String fieldName, Class<T> clazz) {
        try {
            if (!fieldName.contains(".")) return new ArrayList<>(List.of( clazz,(Class<T>) clazz.getDeclaredField(fieldName).getType() ));

            String[] parts = fieldName.split("\\.");
            String head = parts[0];
            Field field = clazz.getDeclaredField(head);
            Class<T> type = (Class<T>) field.getType();

            String tail = String.join(".", Arrays.copyOfRange(parts, 1, parts.length));
            List<Class<T>> result = findFieldRecursively(tail, type);
            result.addFirst(clazz); // add the current FK holder

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> prefetchRelated(String relatedName) {
        List<RelationMeta> relationMetas = ModelCache.relatedModels.get(modelClass);

        for(RelationMeta relationMeta: relationMetas) {
            if (relationMeta.relatedName().equals(relatedName)) {
                Class<? extends Model> referencingModel = relationMeta.referencingModel();

                String referencingModelTableName = ModelInspector.resolveTableName(referencingModel);
                List<Column> referencingModelColumns = ModelInspector.getColumns(referencingModel).stream().map(ColumnInfo::column).toList();
                String referencingModelAliasColumnsScript = GenerateSQLScripts.generateAliasColumns(
                        referencingModelTableName.toLowerCase(),
                        referencingModelColumns
                );

                String modelTableName = ModelInspector.resolveTableName(modelClass);
                List<Column> modelColumns = ModelInspector.getColumns(modelClass).stream().map(ColumnInfo::column).toList();

                String modelAliasColumnsScript = GenerateSQLScripts.generateAliasColumns(
                        modelTableName.toLowerCase(),
                        modelColumns
                );

                String joinOnScript =
                        GenerateSQLScripts.generateJoinOnScript(
                                referencingModelTableName,
                                relationMeta.referencingFieldName(),
                                modelTableName,
                                modelTableName.toLowerCase(),
                                ModelCache.pkUtilMap.get(modelClass).pkName()
                        );

                String resultScript = "SELECT " +
                        referencingModelAliasColumnsScript + "," +
                        modelAliasColumnsScript + " FROM " + referencingModelTableName +
                        " AS " + referencingModelTableName.toLowerCase() + " " + joinOnScript;


                List<Map<String, Object>> rows = DatabaseManager.getInstance().executeSelectQuery(
                        resultScript, null
                );

                return (List<T>) Related.fillCache(rows, relationMeta.referencingFieldName(), modelClass, referencingModel);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Model> hydrateSelectRelatedInstance(List<Map<String, Object>> rows, List<Class<T>> tableHierarchy) {
        // {article_id=1, article_author_id=1,
        // article_content=A story of an old fisherman..., comments_id=1, author_id=1,
        // author_authorName=Ernest Hemingway, comments_comment=Great article!,
        // comments_article_id=1}

        if (rows.isEmpty()) return null;

        Map<Class<T>, List<String>> classToCols = new HashMap<>();
        Map<Class<T>, Map<Object, Model>> caches = new HashMap<>();
        Map<Class<T>, String> classToTableName = new HashMap<>();
        List<String> oneColInstance = rows.getFirst().keySet().stream().toList();

        for(Class<T> clazz: tableHierarchy) {
            String tableName = ModelInspector.resolveTableName(clazz);
            classToTableName.put(clazz, tableName.toLowerCase());
            caches.put(clazz, new HashMap<>());
//
            List<String> filteredRows = oneColInstance.stream()
                    .filter(colName -> colName.startsWith(tableName.toLowerCase() + "_"))
                    .map(colName -> colName.substring(tableName.length() + 1)) // remove prefix and underscore
                    .toList();
//
            classToCols.put(clazz, filteredRows);
        }

        for(Map<String, Object> row: rows) {
            //
            for (Class<T> clazz: tableHierarchy) {
                List<String> cols = classToCols.get(clazz);

                List<String> foreignKeyNames = ModelCache.foreignKeyMap.get(clazz).stream().
                        map(info -> info.column().name()).toList();


                Map<String, Object> singleModelData = new HashMap<>();
                for(String col: cols) {
                    if (foreignKeyNames.contains(col)) {
                        Object pk = row.get(classToTableName.get(clazz) + "_" + col);

                        Class<T> reference = null;

                        var foreignKeys = ModelCache.foreignKeyMap.get(clazz);
                        for(ColumnInfo info: foreignKeys) {
                            if(info.column().name().equals(col)){
                                reference = (Class<T>) info.foreignKey().reference();
                                break;
                            }
                        }

                        singleModelData.put(col, caches.get(reference).get(pk));
                    }

                    else{
                        singleModelData.put(col, row.get(classToTableName.get(clazz) + "_" + col));
                    }
                }

                Object pk = singleModelData.get(
                        ModelCache.pkUtilMap.get(clazz).pkName()
                );

                Model instance = caches.get(clazz).get(pk);
                if (instance == null) {
                    instance = hydrateOneModel(clazz, singleModelData);
                    caches.get(clazz).put(pk, instance);
                }
            }
        }

        Class<T> rootClass = tableHierarchy.getLast();
        return new ArrayList<>(caches.get(rootClass).values());
    }

    private Model hydrateOneModel(Class<T> clazz, Map<String,Object> data) {
        try{
            T instance = clazz.getDeclaredConstructor().newInstance();
            for(ColumnInfo col : ModelInspector.getColumns(clazz)) {
                Object value = data.get(col.column().name());
                Field f = col.field();
                f.setAccessible(true);
                f.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> selectRelated(String fkField) {
        if (fkField.isEmpty()) return null;

        try {
            StringBuilder selectColumns = new StringBuilder();
            StringBuilder joinOnScript = new StringBuilder();

            Set<String> seenAliases = new HashSet<>();
            List<Class<T>> chainOfTables = findFieldRecursively(fkField, modelClass);

            for (int i = 0; i < chainOfTables.size(); i++) {
                Class<T> clazz = chainOfTables.get(i);
                String tableAlias = ModelInspector.resolveTableName(clazz).toLowerCase();

                if (seenAliases.add(tableAlias)) {
                    List<Column> columns = ModelInspector.getColumns(clazz).stream().map(ColumnInfo::column).toList();

                    if (!selectColumns.isEmpty()) selectColumns.append(", ");
                    selectColumns.append(GenerateSQLScripts.generateAliasColumns(tableAlias, columns));
                }

                if (i < chainOfTables.size() - 1) {
                    Class<T> referencedClass = chainOfTables.get(i + 1);
                    String referencedClassName = ModelInspector.resolveTableName(referencedClass);


                    String fkColumnName = "";

                    List<ColumnInfo> infos = ModelCache.foreignKeyMap.get(clazz);

                    for(ColumnInfo info: infos) {
                        if (info.foreignKey().reference().equals(referencedClass)) {
                            fkColumnName = info.column().name();
                        }
                    }

                    joinOnScript.append(
                            GenerateSQLScripts.generateJoinOnScript(
                                    tableAlias,
                                    fkColumnName,
                                    referencedClassName,
                                    referencedClassName.toLowerCase(),
                                    ModelCache.pkUtilMap.get(clazz).pkName()
                            )
                    ).append(" ");
                }
            }

            String baseTable = ModelInspector.resolveTableName(modelClass);
            String baseAlias = baseTable.toLowerCase();

            String script = "SELECT " + selectColumns + " FROM " + baseTable + " AS " + baseAlias + " " + joinOnScript;
            System.out.println(script);
            List<Map<String, Object>> data = DatabaseManager.getInstance().executeSelectQuery(script, null);

            hydrateSelectRelatedInstance(data, chainOfTables.reversed());

            return (List<T>) hydrateSelectRelatedInstance(data, chainOfTables.reversed());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getAll() {
        List<T> rows = new ArrayList<>();
        String selectAllScript = GenerateSQLScripts.getAllRowsScript(this.tableName);
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectQuery(selectAllScript, null);

        for(Map<String, Object> data: results) {
            rows.add(hydrateSingleInstance(data));
        }

        return rows;
    }

    public boolean exists(Filter filter) {
        String script = GenerateSQLScripts.generateExistsScript(tableName, filter.toSQL());
        return DatabaseManager.getInstance().hasResults(script, filter.getParameters());
    }

    // returns number of all existing rows in the table.
    public int countAll() {
        String script = GenerateSQLScripts.generateCountAllScript(tableName);
        return DatabaseManager.getInstance().executeAndReturnCount(script, null);
    }

    public int count(Filter filter) {
        if (filter == null) throw new
                NullFilterException("Filter cannot be null, if you want to count all rows, use countAll() method.");

        String script = GenerateSQLScripts.generateCountScript(tableName, filter.toSQL());
        return DatabaseManager.getInstance().executeAndReturnCount(script, filter.getParameters());
    }

    public T get(Filter filter) {
        String script = GenerateSQLScripts.generateSelectScript(tableName, filter.toSQL());
        List<Map<String, Object>> results = DatabaseManager.getInstance().
                executeSelectQuery(script, filter.getParameters());

        if (results.isEmpty()) throw new GetReturnedLessThanOneRowException("Get returned 0 columns, must return exactly one. Use filter instead.");
        if (results.size() > 1) throw new GetReturnedMoreThanOneRowException("Get returned more than one column must return exactly one. Use filter instead.");

        return hydrateSingleInstance(results.getFirst());
    }

    public List<T> filter(Filter f) {
        List<T> rows = new ArrayList<>();
        String script = GenerateSQLScripts.generateSelectScript(tableName, f.toSQL());
        List<Map<String, Object>> results = DatabaseManager.getInstance().executeSelectQuery(script, f.getParameters());

        for(Map<String, Object> data: results) {
            rows.add(hydrateSingleInstance(data));
        }

        return rows;
    }

    public void bulkInsert(Model... instances) {
        Connection conn = DatabaseManager.getInstance().getConnection();

        try {
            conn.setAutoCommit(false); // Begin transaction

            for (Model instance : instances) {
                instance.save();
            }

            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Bulk insert failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true); // Restore auto-commit
            } catch (SQLException ex) {
                System.err.println("Could not reset auto-commit: " + ex.getMessage());
            }
        }
    }

    public int update(Map<String, Object> data, Filter filter) {
        if (filter == null) throw new
                NullFilterException("Filter cannot be null. If you want to update all rows, use updateAll() method.");

        ValueValidator.validate(data, columnInfos);
        TimeStampManager.validateNotManuallyUpdate(data.keySet());

        LinkedHashMap<String, Object> orderedData = new LinkedHashMap<>(data);

        if (ModelInspector.doesTableHaveUpdatedAtField(modelClass)) {
            orderedData.put("updated_at", TimeStampManager.now());
        }

        String script = GenerateSQLScripts.generateUpdateScript(
                tableName,
                orderedData.keySet(),
                orderedData,
                filter.toSQL()
        );

        List<Object> allValues = new ArrayList<>(orderedData.values()
                .stream().filter(Objects::nonNull).toList());

        allValues.addAll(filter.getParameters());

        return DatabaseManager.getInstance().executeUpdate(script,
                allValues);
    }

    public int deleteAll() {
        System.out.println("WARNING: ALL ROWS WILL BE DELETED FROM THE TABLE: " + tableName);
        String script = GenerateSQLScripts.deleteALlScript(tableName);
        return DatabaseManager.getInstance().executeUpdate(script, null);
    }

    public int delete(Filter filter) {
        if (filter == null) throw new NullFilterException("Filter cannot be null. If you want to delete all rows, use deleteAll() method.");

        String script = GenerateSQLScripts.generateDeleteScript(tableName, filter.toSQL());

        return DatabaseManager.getInstance().executeUpdate(script, filter.getParameters());
    }
}
