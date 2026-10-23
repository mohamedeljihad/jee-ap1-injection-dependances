package ma.enset.dao;

import org.springframework.stereotype.Component;

/** Version "base de données" (simulée). */
@Component("dao")
public class DaoImpl implements IDao {
    @Override
    public double getData() {
        System.out.println("Version base de données");
        return 34;
    }
}
