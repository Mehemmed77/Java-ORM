package orm;

import core.Model;
import core.ModelInspector;
import core.ModelParser;
import core.SchemaGuard;
import org.reflections.Reflections;
import annotations.Table;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ORM {
    @SuppressWarnings("unchecked")
    public static void init() {
        Reflections reflections = new Reflections("Models"); // package to scan

        Set<Class<?>> rawModels = reflections.getTypesAnnotatedWith(Table.class);
        Set<Class<? extends Model>> models = rawModels.stream()
                .filter(Model.class::isAssignableFrom)
                .map(model -> (Class<? extends Model>) model)
                .collect(Collectors.toSet());

        List<Class<? extends Model>> createdModels = models.stream().filter(model ->
                SchemaGuard.doesTableExist(
                        ModelInspector.resolveTableName(model)
                )).toList();

        createdModels.forEach(model -> {
            ModelParser tempParser = new ModelParser(model);
            tempParser.parseOnlyPKs();
        });

        createdModels.forEach(ModelInspector::getColumns);
    }
}
