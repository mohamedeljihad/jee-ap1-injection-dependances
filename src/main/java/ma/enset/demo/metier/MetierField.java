package ma.enset.demo.metier;

import ma.enset.dao.IDao;
import ma.enset.framework.annotations.Component;
import ma.enset.framework.annotations.Inject;
import ma.enset.framework.annotations.Qualifier;
import ma.enset.metier.IMetier;

/** c- injection directe dans l'attribut (Field), sans constructeur ni setter */
@Component("metierField")
public class MetierField implements IMetier {
    @Inject
    @Qualifier("daoBase")
    private IDao dao;

    private double coefficient = 1;   // valeur injectable en XML (<field name="coefficient" value="..."/>)

    @Override
    public double calcul() {
        return dao.getData() * 2 * coefficient;
    }
}
