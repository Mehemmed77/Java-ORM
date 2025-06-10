package tests;

import core.Model;
import customErrors.TableAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import database.DatabaseManager;
import tests.tables.PlaceholderTable;
import tests.tables.TestTable;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:C:\\Users\\user\\Desktop\\JavaORM\\src\\test.db";

    @BeforeEach
    public void setup() {
        DatabaseManager.connect(TEST_DB_URL);

        try(Connection connection = DriverManager.getConnection(TEST_DB_URL);
            Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS TestTable");
        }
        catch(SQLException e) {
            fail("Failed to reset test DB: " + e.getMessage());
        }

        Model.createTable(TestTable.class);
    }

    @Test
    public void testSaveInsertsData_correctly() {
        DatabaseManager.connect(TEST_DB_URL);
        TestTable testTable = new TestTable("Turgut", 18);
        testTable.save();

        try(Connection connection = DriverManager.getConnection(TEST_DB_URL)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM TestTable WHERE name = ?");
            stmt.setObject(1, "Turgut");
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next());
            assertEquals("Turgut", rs.getString("name"));
            assertEquals(18, rs.getInt("age"));
        } catch(SQLException e) {
            fail("Error occurred during checking DB: " + e.getMessage());
        }
    }

    @Test
    public void testCreateTable_failsWhenTableAlreadyExists() {
        DatabaseManager.connect(TEST_DB_URL);

        Model.createTable(PlaceholderTable.class);

        assertThrows(TableAlreadyExistsException.class, () -> Model.createTable(PlaceholderTable.class),
                "Expected TableAlreadyExistsException on second table creation");

        Model.dropTable(PlaceholderTable.class);
    }


}
