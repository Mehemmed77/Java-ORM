package migrationManager;

import java.util.*;

import core.Model;

public class ManagerHandler {
    public void setup() {
        MigrationManager migrationManager = new MigrationManager();

        List<LinkedHashMap<String, Object>> parsedModels = new ArrayList<>();

        List<Class<? extends Model>> models = migrationManager.loadModels();
        for(Class<? extends Model> modelClass: models) {
            parsedModels.add(migrationManager.parseModel(modelClass));
        }

        VersionManager.generateNextMigrationFile();
        VersionManager.writeToNewFile(parsedModels);
    }
}
