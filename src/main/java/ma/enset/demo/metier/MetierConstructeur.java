package ma.enset.demo.metier;

import ma.enset.dao.IDao;
import ma.enset.framework.annotations.Component;
import ma.enset.framework.annotations.Inject;
import ma.enset.framework.annotations.Qualifier;
import ma.enset.metier.IMetier;

/** a- injection par le constructeur */
@Component("metierConstructeur")
public class MetierConstructeur implements IMetier {
    private final IDao dao;

    @Inject
    public MetierConstructeur(@Qualifier("daoBase") IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcul() {
        return dao.getData() * 2;
    }
}
