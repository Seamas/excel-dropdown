package wang.seamas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DropDownSetField {

    String[] source() default {};

    Class<? extends DropDownInterface>[] sourceClass() default {};

    String key() default "";

    String deps() default "";

    boolean multiply() default false;
}
