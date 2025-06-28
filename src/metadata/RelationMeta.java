package metadata;

import core.Model;

public record RelationMeta(Class<? extends Model> referencingModel, String referencingFieldName, String relatedName,
                           Class<? extends Model> targetModel) {
}
