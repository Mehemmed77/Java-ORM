package migrationManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class VersionManager {
    public static final String ROOT_PATH = Paths.get("src", "main", "java", "versions").toString();

    private static File latestFile;

    public static String getLatestMigrationFileName() {
        Path path = Paths.get(ROOT_PATH);
        File folder = path.toFile();

        File[] files = folder.listFiles((_, name) -> name.startsWith("migration") && name.endsWith(".json"));

        if(files == null || files.length == 0) return "migration_0001.json";

        Arrays.sort(files, Comparator.comparing(File::getName));

        File lastFile = files[files.length - 1];
        String lastNumStr = lastFile.getName().substring(10, 14); // "0001"
        int nextNum = Integer.parseInt(lastNumStr) + 1;

        String nextNumStr = String.format("%04d", nextNum); // always 4-digit padded
        return "migration_" + nextNumStr + ".json";
    }

    public static void generateNextMigrationFile() {
        Path path = Paths.get(ROOT_PATH);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                System.err.println("Failed to create versions folder: " + e.getMessage());
                return;
            }
        }

        File folder = path.toFile();

        String newFileName = getLatestMigrationFileName();

        try {
            File newFile = new File(folder, newFileName);
            if(newFile.createNewFile()) {
                latestFile = newFile;
                System.out.println("New file created: " + newFile.getPath());
            }
            else{
                System.out.println("File already exists: " + newFile.getPath());
            }
        } catch (Exception e) {
            System.err.println("Error creating new migration file: " + e.getMessage());
        }
    }

    public static void writeToNewFile(List<LinkedHashMap<String, Object>> parsedModels) {
        if(latestFile == null) return;

        WriterManager.write(parsedModels, latestFile);
        WriterManager.getterWriter(parsedModels);
    }
}
