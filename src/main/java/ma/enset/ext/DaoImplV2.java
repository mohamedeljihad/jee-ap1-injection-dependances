package ma.enset.ext;

import ma.enset.dao.IDao;
import org.springframework.stereotype.Component;

/** Nouvelle version (capteurs / web service) ajoutée sans modifier le code existant. */
@Component("dao2")
public class DaoImplV2 implements IDao {
    @Override
    public double getData() {
        System.out.println("Version capteurs");
        return 12;
    }
}
