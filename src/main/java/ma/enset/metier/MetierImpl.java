package ma.enset.metier;

import ma.enset.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Couplage faible : MetierImpl ne dépend que de l'interface IDao.
 * L'implémentation concrète est injectée de l'extérieur (constructeur ou setter).
 */
@Component("metier")
public class MetierImpl implements IMetier {

    private IDao dao;

    public MetierImpl() { }

    /** Injection par constructeur (utilisée par Spring en version annotations). */
    @Autowired
    public MetierImpl(@Qualifier("dao") IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcul() {
        double t = dao.getData();
        return t * 12 * Math.PI / 2 * Math.cos(t);
    }

    /** Injection par setter (utilisée en instanciation statique/dynamique et en XML). */
    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
