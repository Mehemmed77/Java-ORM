package manager;
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
import utils.GenerateSQLScripts;
import utils.TimeStampManager;
import validators.ValueValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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

    private T hydrateSingleInstance(Map<String, Object> data) {
        try {
            T instance = constructor.newInstance();

            for(ColumnInfo col : columnInfos) {
                String columnName = col.column().name();
                Field field = col.field();

                Object value = data.get(columnName);

                if (field.isAnnotationPresent(ForeignKey.class)) {
                    field.set(instance, null);
                    instance.proxyMap.put(field.getName(), value);
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
            result.add(clazz); // add the current FK holder

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String selectRelated(String... fkFields) {
        if (fkFields.length == 0) return "";

        try {
            StringBuilder selectColumns = new StringBuilder();
            StringBuilder joinOnScript = new StringBuilder();

            Set<String> seenAliases = new HashSet<>();

            for (String fkField : fkFields) {
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
            }

            String baseTable = ModelInspector.resolveTableName(modelClass);
            String baseAlias = baseTable.toLowerCase();

            return "SELECT " + selectColumns + " FROM " + baseTable + " AS " + baseAlias + " " + joinOnScript;
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
