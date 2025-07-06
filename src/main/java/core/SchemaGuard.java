package core;
import customErrors.MissingTableException;
import customErrors.NoEmptyParameterConstructorException;
import customErrors.TableAlreadyExistsException;
import database.DatabaseManager;
import utils.GenerateSQLScripts;

import java.lang.reflect.Constructor;

public abstract class SchemaGuard {

    protected static void ensureEmptyConstructorExistsOrThrow(Class<? extends Model> clazz) {
        boolean exists = false;
        for(Constructor<?> constructor: clazz.getDeclaredConstructors()) {
            if(constructor.getParameterCount() == 0){
                exists = true;
                break;
            }
        }

        if (!exists) throw new NoEmptyParameterConstructorException("Model table should have empty constructor.");
    }

    // Helper class that returns if table exists or not.
    public static boolean doesTableExist(String tableName) {
        String script = GenerateSQLScripts.tableExistsScript(tableName);
        return DatabaseManager.getInstance().hasResults(script, null);
    }

    // Throws if table does not exist.
    public static void ensureTableExistsOrThrow(String tableName) {
        if (!doesTableExist(tableName)) throw new MissingTableException("Table named " + tableName + " does not exist");
    }

    // Throws if table exists.
    protected static void ensureTableNotExistsOrThrow(String tableName) {
        if (doesTableExist(tableName)) throw new TableAlreadyExistsException("Table named " + tableName + " already exists");
    }
}
