package ma.enset.demo;

import ma.enset.framework.context.ApplicationContext;
import ma.enset.framework.context.XmlApplicationContext;
import ma.enset.metier.IMetier;

/** Partie 2 : mini framework, version XML (JAXB). */
public class PresFrameworkXML {
    public static void main(String[] args) {
        ApplicationContext ctx = new XmlApplicationContext("mini-beans.xml");
        for (String id : new String[]{"metierConstructeur", "metierSetter", "metierField"}) {
            IMetier metier = (IMetier) ctx.getBean(id);
            System.out.println(id + " -> résultat = " + metier.calcul());
        }
    }
}
