package ma.enset.presentation;

import ma.enset.dao.IDao;
import ma.enset.metier.IMetier;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * 5.b Injection par instanciation dynamique : les noms des classes sont lus dans config.txt,
 * les objets sont créés par réflexion. Changer d'implémentation = modifier config.txt, sans recompiler.
 */
public class PresDynamique {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(new File(args.length > 0 ? args[0] : "config.txt"));

        String daoClassName = scanner.nextLine().trim();
        Class<?> cDao = Class.forName(daoClassName);
        IDao dao = (IDao) cDao.getDeclaredConstructor().newInstance();

        String metierClassName = scanner.nextLine().trim();
        Class<?> cMetier = Class.forName(metierClassName);
        IMetier metier = (IMetier) cMetier.getDeclaredConstructor().newInstance();

        Method setDao = cMetier.getMethod("setDao", IDao.class);
        setDao.invoke(metier, dao);          // metier.setDao(dao)

        System.out.println("Résultat = " + metier.calcul());
        scanner.close();
    }
}
