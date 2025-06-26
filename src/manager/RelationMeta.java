package manager;

import core.Model;

public class RelationMeta {
    public final Class<? extends Model> referencingModel;
    public final String referencingFieldName;
    public final String relatedName;
    public final Class<? extends Model> targetModel;

    public RelationMeta(Class<? extends Model> referencingModel,
                        String referencingFieldName,
                        String relatedName,
                        Class<? extends Model> targetModel) {

        this.referencingModel = referencingModel;
        this.referencingFieldName = referencingFieldName;
        this.relatedName = relatedName;
        this.targetModel = targetModel;
    }
}
