package ma.enset.framework.annotations;

import java.lang.annotation.*;

/** Choisit le bean à injecter par son nom quand plusieurs implémentations existent. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Qualifier {
    String value();
}
