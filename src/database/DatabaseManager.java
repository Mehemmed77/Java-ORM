package database;
import enums.ScriptResults;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String url = "jdbc:sqlite:C:\\Users\\user\\Desktop\\JavaORM\\src\\orm.db";
    private static Connection connection;
    private static DatabaseManager instance;

    private DatabaseManager() {
        System.out.println("DatabaseManager is Instantiated");
        connect();
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

    public static void connect() {
        try{
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

    // This is for table creation.
    public ScriptResults executeCommand(String script) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(script);
            return ScriptResults.SUCCESS;
        }

        catch (SQLException e) {
            System.out.println(e.getMessage());
            return ScriptResults.FAIL;
        }
    }
}
