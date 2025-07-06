package tests;
import core.Model;
import core.ModelInspector;
import org.junit.jupiter.api.Test;
import tests.tables.DummyTable;
import utils.GenerateSQLScripts;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class GenerateSQLScriptsTest {
    @Test
    public void testCreateTableScript_containsAllConstraints() {
        String generatedCreateTableScript = GenerateSQLScripts.createTableScript("DummyTable",
                ModelInspector.getColumns(DummyTable.class));

        String scriptToBeCompared = "CREATE TABLE DummyTable (id INTEGER PRIMARY KEY AUTOINCREMENT ,username varchar(255) NOT NULL UNIQUE ,age INTEGER NOT NULL )";

        assertEquals(generatedCreateTableScript, scriptToBeCompared);
    }

    @Test
    public void testGenerateInsertScript_hasCorrectSyntax() {
        List<String> keys = List.of("username", "age");
        String insertScript = GenerateSQLScripts.generateParameterizedInsert("DummyTable", keys);

        String insertScriptToBeCompared = "INSERT INTO DummyTable (username,age) VALUES (?,?)";

        assertEquals(insertScript, insertScriptToBeCompared);
    }
}
