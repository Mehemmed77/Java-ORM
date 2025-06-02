package annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
    String name();
    String type();
    boolean primaryKey() default false;
    boolean nullable() default true;
    boolean unique() default false;
    String defaultValue() default "LOREM IPSUM";
}
