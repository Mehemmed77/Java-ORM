package annotations;

import core.Model;
import enums.ReferentialAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ForeignKey {
    Class<? extends Model> reference();
    String relatedName() default "";
    boolean nullable() default true;
    ReferentialAction onDelete() default ReferentialAction.CASCADE; // optional: CASCADE, SET NULL, etc.
    ReferentialAction onUpdate() default ReferentialAction.CASCADE; // optional: CASCADE, SET NULL, etc.
}
