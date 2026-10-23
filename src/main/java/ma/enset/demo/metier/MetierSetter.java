package ma.enset.demo.metier;

import ma.enset.dao.IDao;
import ma.enset.framework.annotations.Component;
import ma.enset.framework.annotations.Inject;
import ma.enset.framework.annotations.Qualifier;
import ma.enset.metier.IMetier;

/** b- injection par le setter */
@Component("metierSetter")
public class MetierSetter implements IMetier {
    private IDao dao;

    public MetierSetter() { }

    /** utilisé aussi par la version XML (<constructor-arg>) */
    public MetierSetter(IDao dao) { this.dao = dao; }

    @Inject
    @Qualifier("daoCapteur")
    public void setDao(IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcul() {
        return dao.getData() * 2;
    }
}
