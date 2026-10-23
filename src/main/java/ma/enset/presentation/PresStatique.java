package ma.enset.presentation;

import ma.enset.dao.DaoImpl;
import ma.enset.metier.MetierImpl;

/** 5.a Injection des dépendances par instanciation statique (new). */
public class PresStatique {
    public static void main(String[] args) {
        DaoImpl dao = new DaoImpl();
        MetierImpl metier = new MetierImpl(dao);   // injection via le constructeur
        // ou : MetierImpl metier = new MetierImpl(); metier.setDao(dao);  (via le setter)
        System.out.println("Résultat = " + metier.calcul());
    }
}
