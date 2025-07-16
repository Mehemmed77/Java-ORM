package migrationManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WriterManager {
    private static final String MODEL_PATH = Paths.get("src", "main", "java", "Models").toString();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void write(List<LinkedHashMap<String, Object>> snapshot, File file) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, snapshot);
            System.out.println("Migration snapshot saved to " + file.getPath());
        } catch (Exception e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }

    public static void getterWriter(List<LinkedHashMap<String, Object>> snapshot) {
        Path path = Paths.get(MODEL_PATH);
        File folder = path.toFile();
        File[] files = folder.listFiles();

        assert files != null;

        for(File file: files) {
            String modelFileName = file.getAbsoluteFile().getName();
            for(LinkedHashMap<String, Object> data: snapshot) {
                if (modelFileName.equals(data.get("modelName") + ".java")) {
                    List<Map<String, Object>> listOfGetters = (List<Map<String, Object>>) data.get("getters");

                    StringBuilder getterString = new StringBuilder();
                    for (Map<String, Object> map: listOfGetters) {
                        getterString.append(generateGetterString(map)).append("\n");
                    }

                    if (!getterString.isEmpty()) {
                        writeGetterString(getterString.toString(), file.getPath());
                    }

                    break;
                }
            }
        }
    }

    public static void writeGetterString(String getterString, String filePath) {
        try {
            Path path = Paths.get(filePath);
            String code = Files.readString(path);

            int insertIndex = code.lastIndexOf('}');

            String newCode = code.substring(0, insertIndex) + getterString + "\n}";
            Files.writeString(path, newCode);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static String generateGetterString(Map<String, Object> getter) {
        String indent = "    ";
        return String.format("""
                
                %spublic %s %s() {
                    %sreturn %s;
                %s}""", indent,
                getter.get("returnType"),
                getter.get("getterName"),
                indent,
                getter.get("returnValue"),
                indent);
    }
}
