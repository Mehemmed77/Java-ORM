package tests;

import core.Model;
import customErrors.MissingPrimaryKeyException;
import customErrors.MultiplePrimaryKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import database.DatabaseManager;
import tests.tables.TableWithInvalidColumnName;
import tests.tables.TableWithMoreThanOnePK;
import tests.tables.TableWithNoPrimaryKey;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:C:\\Users\\user\\Desktop\\JavaORM\\src\\test.db";

    @BeforeEach
    public void setup() {
        DatabaseManager.connect(TEST_DB_URL);
    }

    @Test
    public void testModelWithoutPrimaryKey_throws() {
        assertThrows(MissingPrimaryKeyException.class,
                () -> Model.createTable(TableWithNoPrimaryKey.class),
                "Expected Missing Primary Key Exception.");
    }

    @Test
    public void testModelWithMultiplePrimaryKey_throws() {
        assertThrows(MultiplePrimaryKeyException.class,
                () -> Model.createTable(TableWithMoreThanOnePK.class),
                "Expected Multiple Primary Key Exception.");
    }

    @Test
    public void testModelWithInvalidColumnName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Model.createTable(TableWithInvalidColumnName.class),
                "Expected Illegal Argument error.");
    }
}
