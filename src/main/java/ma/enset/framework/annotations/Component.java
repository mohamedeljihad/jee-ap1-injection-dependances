package ma.enset.framework.annotations;

import java.lang.annotation.*;

/** Marque une classe comme composant géré par le mini framework. value = nom du bean. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    String value() default "";
}
