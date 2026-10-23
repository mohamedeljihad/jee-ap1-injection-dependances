package ma.enset.demo;

import ma.enset.framework.context.AnnotationApplicationContext;
import ma.enset.metier.IMetier;

/** Partie 2 : mini framework, version annotations (@Component / @Inject / @Qualifier). */
public class PresFrameworkAnnotations {
    public static void main(String[] args) {
        AnnotationApplicationContext ctx = new AnnotationApplicationContext("ma.enset.demo");
        System.out.println("Composants détectés : " + ctx.getBeanNames());
        for (String id : new String[]{"metierConstructeur", "metierSetter", "metierField"}) {
            IMetier metier = (IMetier) ctx.getBean(id);
            System.out.println(id + " -> résultat = " + metier.calcul());
        }
    }
}
