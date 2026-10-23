package ma.enset.framework.annotations;

import java.lang.annotation.*;

/** Demande l'injection d'une dépendance via un constructeur, un setter ou directement un attribut. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
public @interface Inject {
}
