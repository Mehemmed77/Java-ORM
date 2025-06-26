package metadata;

import core.Model;

public record ReverseRelationMap(Class<? extends Model> referencingModel, String referencingFieldName, String relatedName) {
}
