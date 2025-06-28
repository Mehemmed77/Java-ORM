package orm;

import core.Model;
import core.ModelInspector;
import core.SchemaGuard;
import org.reflections.Reflections;
import annotations.Table;

public class ORM {
    @SuppressWarnings("unchecked")
    public static void init() {
        Reflections reflections = new Reflections("Models"); // package to scan
        var models = reflections.getTypesAnnotatedWith(Table.class);
        for (Class<?> modelClass : models) {
            if (Model.class.isAssignableFrom(modelClass) &&
                    SchemaGuard.doesTableExist(
                            ModelInspector.resolveTableName((Class<? extends Model>) modelClass))) {

                Class<? extends Model> typedModel = (Class<? extends Model>) modelClass;
                ModelInspector.getColumns(typedModel);
            }
        }
    }
}
