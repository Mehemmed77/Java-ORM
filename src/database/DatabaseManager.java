package database;
import core.Model;
import core.ModelInspector;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Singleton class responsible for managing the database connection and executing queries.
public class DatabaseManager {
    private static String url = "jdbc:sqlite:./src/orm.db";
    private static Connection connection;
    private static DatabaseManager instance;

    // Private constructor ensures singleton instantiation.
    private DatabaseManager() {
        System.out.println("DatabaseManager is Instantiated");
    }

    // Returns the singleton instance and ensures connection is active.
    public static DatabaseManager getInstance() {
        if (instance == null){
            instance = new DatabaseManager();
        }
        connect();

        return instance;
    }

    // Ensures DB connection is gracefully closed on JVM shutdown.
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (connection != null) {
                System.out.println("Closing DB connection gracefully...");
                close();
            }
        }));
    }

    // Sets a new database URL and reconnects.
    public static void connect(String newUrl) {
        url = newUrl;
        connect();
    }

    // Connects to the database using the current URL.
    public static void connect() {
        try{
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    // Closes the database connection.
    public static void close() {
        try{
            connection.close();
        }
        catch (SQLException e) {
            System.err.println("Failed to close the connection: " + e.getMessage());
        }
    }

    public int executeSave(Class<? extends Model> clazz, String script, List<Object> values){
        try {
            try (PreparedStatement stmt = connection.prepareStatement(script, Statement.RETURN_GENERATED_KEYS)) {
                System.out.println("Executing: " + script);
                System.out.println("Values: " + values);

                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }


                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(ModelInspector.getPkIndex(clazz)); // return generated ID
                    }
                }


            }
        } catch (SQLException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return -1;
    }

    // Executes a raw SQL command (CREATE, DROP, DELETE) with and without parameters.
    public int executeUpdate(String script, List<Object> values) {
        try {
            if (values == null || values.isEmpty()) {
                try (Statement statement = connection.createStatement()) {
                    return statement.executeUpdate(script);
                }
            }
            else {
                try (PreparedStatement stmt = connection.prepareStatement(script)) {
                    System.out.println("Executing: " + script);
                    System.out.println("Values: " + values);

                    for (int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                    }

                    int affected = stmt.executeUpdate();

                    return affected;
                }
            }
        } catch (SQLException e) {
            System.err.println("Update failed: " + e.getMessage());
        }

        return 0;
    }

    public List<Map<String, Object>> executeSelectQuery(String script, List<Object> values) {
        try {
            if (values == null || values.isEmpty()) {
                try(Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(script);
                    return convertResultSetToListMap(rs);
                }
            }

            else{
                try(PreparedStatement stmt = connection.prepareStatement(script)) {
                    for(int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                    }

                    return  convertResultSetToListMap(stmt.executeQuery());
                }
            }
        }

        catch (SQLException e) {
            System.out.println("Select Failed: " + e.getMessage());
        }

        return null;
    }

    // Executes a SELECT query and returns true if any rows exist.
    public boolean hasResults(String script, List<Object> values) {
        try{
            if(values == null || values.isEmpty()) {
                try(Statement stmt = connection.createStatement();){
                    return stmt.executeQuery(script).next();
                }
            }
            else{
                try(PreparedStatement stmt = connection.prepareStatement(script)){
                    for(int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                    }

                    return stmt.executeQuery().next();
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public int executeAndReturnCount(String script, List<Object> values) {
        try{
            if (values == null || values.isEmpty()) {
                try(Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(script);
                    rs.next();
                    return rs.getInt(1);
                }
            }

            else{
                try(PreparedStatement stmt = connection.prepareStatement(script)){
                    for(int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }

        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    // Converts a ResultSet into a list of maps (column name → value).
    private List<Map<String, Object>> convertResultSetToListMap(ResultSet rs) {
        List<Map<String, Object>> results = new ArrayList<>();
        try{
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for(int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        catch (SQLException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

        return results;
    }
}
