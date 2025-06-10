package database;
import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private static String url = "jdbc:sqlite:C:\\Users\\user\\Desktop\\JavaORM\\src\\orm.db";
    private static Connection connection;
    private static DatabaseManager instance;

    private DatabaseManager() {
        System.out.println("DatabaseManager is Instantiated");
    }

    public static DatabaseManager getInstance() {
        if (instance == null){
            instance = new DatabaseManager();
        }

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
