package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String url = "jdbc:sqlite:../orm.db";
    private static Connection connection;

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
}
