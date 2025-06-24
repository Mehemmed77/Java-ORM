package annotations;

import core.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ForeingKey {
    Class<? extends Model> reference();
    String column() default "id";
    boolean nullable() default true;
}
