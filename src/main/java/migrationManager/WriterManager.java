package migrationManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WriterManager {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void write(List<LinkedHashMap<String, Object>> snapshot, File file) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, snapshot);
            System.out.println("Migration snapshot saved to " + file.getPath());
        } catch (Exception e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }
}
