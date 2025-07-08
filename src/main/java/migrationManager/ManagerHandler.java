package migrationManager;

import java.util.List;

import core.Model;

public class ManagerHandler {
    public void setup() {
        MigrationManager migrationManager = new MigrationManager();
        List<Class<? extends Model>> models = migrationManager.loadModels();

        for(Class<? extends Model> modelClass: models) {
            migrationManager.parseModel(modelClass);
        }
    }
}
