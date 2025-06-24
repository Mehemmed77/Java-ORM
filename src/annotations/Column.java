package annotations;
import enums.ColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
    String name();
    ColumnType type();
    boolean nullable() default true;
    boolean unique() default false;
    int length() default 255;
    String defaultValue() default "LOREM IPSUM";
}
