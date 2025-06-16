package database;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static String url = "jdbc:sqlite:./src/orm.db";
    private static Connection connection;
    private static DatabaseManager instance;

    private DatabaseManager() {
        System.out.println("DatabaseManager is Instantiated");
    }

    public static DatabaseManager getInstance() {
        if (instance == null){
            instance = new DatabaseManager();
        }
        connect();

        return instance;
    }

    // to avoid manually shutting down the browser.
    // this method is always going to run making sure that connection is closed.
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isConnected()) {
                System.out.println("Closing DB connection gracefully...");
                close();
            }
        }));
    }

    public static void connect(String newUrl) {
        url = newUrl;
        connect();
    }

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

    public static boolean isConnected() {
        return connection != null;
    }

    public static void close() {
        try{
            connection.close();
        }
        catch (SQLException e) {
            System.err.println("Failed to close the connection: " + e.getMessage());
        }
    }

    // This method serves to just execute and nothing else.
    public void executeCommand(String script) {
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate(script);
        }

        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // This method serves to just execute and nothing else.
    public boolean executeCommandAndReturn(String script) {
        try(Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(script);

            return rs.next();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

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

    public List<Map<String, Object>> executeSelectAndFetch(String script) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(script)) {

            return convertResultSetToListMap(rs);

        } catch (SQLException e) {
            System.out.println("Select failed: " + e.getMessage());
        }

        return null;
    }

    public List<Map<String, Object>> executePreparedSelectAndFetch(List<Object> values, String script) {
        try(PreparedStatement stmt = connection.prepareStatement(script)) {
            System.out.println("Preparing: " + script);

            for(int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            return convertResultSetToListMap(stmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Parametrization failed " + e.getMessage());
        }

        return null;
    }

    public void executeInsert(List<Object> values, String script) {
        try(PreparedStatement stmt = connection.prepareStatement(script)) {
            System.out.println("Executing: " + script);
            System.out.println("Values: " + values);

            for(int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println("Insert failed: " + e.getMessage());
        }
    }
}
